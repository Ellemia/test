import { chromium, type Browser } from 'playwright';
import { RENDERER_LIMITS } from '@webshot/shared';

let browserPromise: Promise<Browser> | null = null;

export async function getBrowser(): Promise<Browser> {
  if (!browserPromise) {
    // Some deployments run the renderer behind a corporate/egress proxy.
    // Unset by default — most deployments have direct outbound internet access.
    const proxyServer = process.env.RENDERER_HTTP_PROXY;
    const args = ['--disable-dev-shm-usage'];
    // NEVER set this in production: it disables TLS certificate validation for
    // every user-submitted URL, so a MITM'd or spoofed site would go undetected.
    // Exists only so this renderer can be smoke-tested behind a dev sandbox's own
    // TLS-intercepting proxy, which presents a cert Chromium doesn't trust.
    if (process.env.RENDERER_INSECURE_IGNORE_CERT_ERRORS === 'true') {
      args.push('--ignore-certificate-errors');
    }
    browserPromise = chromium.launch({
      headless: true,
      args,
      proxy: proxyServer ? { server: proxyServer } : undefined,
    });
  }
  return browserPromise;
}

export async function closeBrowser(): Promise<void> {
  if (!browserPromise) return;
  const browser = await browserPromise;
  browserPromise = null;
  await browser.close();
}

/** Simple counting semaphore so one slow/malicious page can't monopolize the renderer. */
class Semaphore {
  private available: number;
  private queue: Array<() => void> = [];

  constructor(max: number) {
    this.available = max;
  }

  async acquire(): Promise<() => void> {
    if (this.available > 0) {
      this.available -= 1;
      return this.release.bind(this);
    }
    await new Promise<void>((resolve) => this.queue.push(resolve));
    this.available -= 1;
    return this.release.bind(this);
  }

  private release(): void {
    this.available += 1;
    const next = this.queue.shift();
    if (next) next();
  }

  get pendingCount(): number {
    return this.queue.length;
  }
}

export const renderSemaphore = new Semaphore(RENDERER_LIMITS.maxConcurrentRenders);
