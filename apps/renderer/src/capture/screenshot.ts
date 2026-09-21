import sharp from 'sharp';
import type { CaptureRequest } from '@webshot/shared';
import { ERROR_CODES, MAX_PAGE_HEIGHT, RENDERER_LIMITS } from '@webshot/shared';
import { getBrowser } from '../browser/pool.js';
import { assertUrlIsSafe, SsrfError } from '../security/ssrf.js';

export class CaptureError extends Error {
  code: string;
  constructor(code: string, message: string) {
    super(message);
    this.name = 'CaptureError';
    this.code = code;
  }
}

export interface CaptureResult {
  buffer: Buffer;
  width: number;
  height: number;
  format: 'png' | 'webp';
  durationMs: number;
}

export async function captureScreenshot(req: CaptureRequest): Promise<CaptureResult> {
  const startedAt = Date.now();

  // Validate before ever touching the network.
  await assertUrlIsSafe(req.url).catch((err) => {
    throw err instanceof SsrfError ? err : new CaptureError(ERROR_CODES.INVALID_URL, 'The URL is not allowed.');
  });

  const browser = await getBrowser();
  const context = await browser.newContext({
    viewport: { width: req.viewport.width, height: req.viewport.height },
    deviceScaleFactor: req.viewport.deviceScaleFactor,
    javaScriptEnabled: true,
  });

  // Redirect / DNS-rebinding defense: re-validate every navigation hop, not just
  // the URL the client sent us — a first hop can be safe and a later hop private.
  await context.route('**/*', async (route) => {
    const request = route.request();
    if (!request.isNavigationRequest()) {
      await route.continue();
      return;
    }
    try {
      await assertUrlIsSafe(request.url());
      await route.continue();
    } catch {
      await route.abort('blockedbyclient');
    }
  });

  try {
    const page = await context.newPage();
    page.setDefaultNavigationTimeout(RENDERER_LIMITS.navigationTimeoutMs);
    page.setDefaultTimeout(RENDERER_LIMITS.screenshotTimeoutMs);

    try {
      await page.goto(req.url, {
        waitUntil: 'domcontentloaded',
        timeout: RENDERER_LIMITS.navigationTimeoutMs,
      });
    } catch (err) {
      if (err instanceof Error && /blockedbyclient/i.test(err.message)) {
        throw new SsrfError(ERROR_CODES.SSRF_BLOCKED, 'This URL cannot be accessed for security reasons.');
      }
      throw new CaptureError(ERROR_CODES.PAGE_TIMEOUT, 'The target page took too long to respond.');
    }

    await page
      .waitForLoadState('networkidle', { timeout: RENDERER_LIMITS.networkIdleTimeoutMs })
      .catch(() => {
        /* ads/analytics can keep the network busy forever — proceed anyway */
      });

    await page.evaluate(() => document.fonts?.ready).catch(() => {});

    if (req.delay > 0) {
      await page.waitForTimeout(req.delay);
    }

    if (req.fullPage) {
      const pageHeight = await page.evaluate(() => document.documentElement.scrollHeight);
      if (pageHeight > MAX_PAGE_HEIGHT) {
        throw new CaptureError(ERROR_CODES.PAGE_TOO_LARGE, `Page height exceeds the ${MAX_PAGE_HEIGHT}px limit.`);
      }
    }

    let pngBuffer: Buffer;
    try {
      // Playwright's screenshot() only supports png/jpeg natively; webp is
      // produced below by re-encoding the PNG with sharp.
      pngBuffer = await page.screenshot({
        fullPage: req.fullPage,
        type: 'png',
        timeout: RENDERER_LIMITS.screenshotTimeoutMs,
      });
    } catch (err) {
      const isTimeout = err instanceof Error && /timeout/i.test(err.message);
      console.error('screenshot capture failed', err);
      throw new CaptureError(
        isTimeout ? ERROR_CODES.SCREENSHOT_TIMEOUT : ERROR_CODES.INTERNAL_ERROR,
        isTimeout ? 'Taking the screenshot took too long.' : 'Screenshot service is temporarily unavailable.',
      );
    }

    let buffer = pngBuffer;
    if (req.format === 'webp') {
      try {
        buffer = await sharp(pngBuffer).webp({ quality: 90 }).toBuffer();
      } catch (err) {
        console.error('webp conversion failed', err);
        throw new CaptureError(ERROR_CODES.INTERNAL_ERROR, 'Screenshot service is temporarily unavailable.');
      }
    }

    const viewportSize = page.viewportSize();
    return {
      buffer,
      width: viewportSize?.width ?? req.viewport.width,
      height: req.fullPage ? await page.evaluate(() => document.documentElement.scrollHeight) : (viewportSize?.height ?? req.viewport.height),
      format: req.format,
      durationMs: Date.now() - startedAt,
    };
  } finally {
    await context.close();
  }
}
