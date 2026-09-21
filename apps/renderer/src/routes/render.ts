import type { Request, Response } from 'express';
import { captureRequestSchema, ERROR_CODES, type ApiErrorBody } from '@webshot/shared';
import { captureScreenshot, CaptureError } from '../capture/screenshot.js';
import { SsrfError } from '../security/ssrf.js';
import { renderSemaphore } from '../browser/pool.js';

function sendError(res: Response, status: number, code: string, message: string): void {
  const body: ApiErrorBody = { error: { code, message } };
  res.status(status).json(body);
}

export async function renderHandler(req: Request, res: Response): Promise<void> {
  const parsed = captureRequestSchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, 400, ERROR_CODES.INVALID_URL, parsed.error.issues[0]?.message ?? 'Invalid request body.');
    return;
  }

  if (renderSemaphore.pendingCount >= 8) {
    sendError(res, 503, ERROR_CODES.RENDERER_BUSY, 'The renderer is busy. Try again shortly.');
    return;
  }

  const release = await renderSemaphore.acquire();

  try {
    const result = await withOverallTimeout(captureScreenshot(parsed.data), 30_000);
    res.setHeader('Content-Type', result.format === 'png' ? 'image/png' : 'image/webp');
    res.setHeader('Cache-Control', 'no-store');
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('X-Capture-Width', String(result.width));
    res.setHeader('X-Capture-Height', String(result.height));
    res.setHeader('X-Capture-Duration-Ms', String(result.durationMs));
    res.status(200).send(result.buffer);
  } catch (err) {
    if (err instanceof SsrfError) {
      sendError(res, 400, err.code, err.message);
    } else if (err instanceof CaptureError) {
      sendError(res, 502, err.code, err.message);
    } else if (err instanceof Error && err.message === 'OVERALL_TIMEOUT') {
      sendError(res, 504, ERROR_CODES.PAGE_TIMEOUT, 'The request took too long overall.');
    } else {
      sendError(res, 500, ERROR_CODES.INTERNAL_ERROR, 'Screenshot service is temporarily unavailable.');
    }
  } finally {
    release();
  }
}

function withOverallTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error('OVERALL_TIMEOUT')), ms);
    promise
      .then((value) => {
        clearTimeout(timer);
        resolve(value);
      })
      .catch((err) => {
        clearTimeout(timer);
        reject(err);
      });
  });
}
