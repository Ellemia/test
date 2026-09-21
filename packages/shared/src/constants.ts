export const DEVICE_PRESETS = {
  desktop: { label: 'Desktop', width: 1440, height: 900, deviceScaleFactor: 1 },
  laptop: { label: 'Laptop', width: 1280, height: 800, deviceScaleFactor: 1 },
  tablet: { label: 'Tablet', width: 768, height: 1024, deviceScaleFactor: 1 },
  mobile: { label: 'Mobile', width: 390, height: 844, deviceScaleFactor: 2 },
} as const;

export type DevicePresetId = keyof typeof DEVICE_PRESETS;

export const VIEWPORT_LIMITS = {
  minWidth: 320,
  maxWidth: 3840,
  minHeight: 320,
  maxHeight: 10000,
  minDeviceScaleFactor: 1,
  maxDeviceScaleFactor: 3,
} as const;

export const MAX_PAGE_HEIGHT = 30_000;

export const ALLOWED_DELAYS_MS = [0, 500, 1000, 2000, 3000] as const;
export const MAX_DELAY_MS = 3000;

export const ALLOWED_FORMATS = ['png', 'webp'] as const;
export type ImageFormat = (typeof ALLOWED_FORMATS)[number];

export const ALLOWED_PROTOCOLS = ['http:', 'https:'] as const;

export const RENDERER_LIMITS = {
  navigationTimeoutMs: 15_000,
  networkIdleTimeoutMs: 5_000,
  screenshotTimeoutMs: 10_000,
  overallRequestTimeoutMs: 30_000,
  maxConcurrentRenders: 3,
} as const;

export const ERROR_CODES = {
  INVALID_URL: 'INVALID_URL',
  UNSUPPORTED_PROTOCOL: 'UNSUPPORTED_PROTOCOL',
  SSRF_BLOCKED: 'SSRF_BLOCKED',
  DNS_RESOLUTION_FAILED: 'DNS_RESOLUTION_FAILED',
  TARGET_UNREACHABLE: 'TARGET_UNREACHABLE',
  PAGE_TIMEOUT: 'PAGE_TIMEOUT',
  SCREENSHOT_TIMEOUT: 'SCREENSHOT_TIMEOUT',
  PAGE_TOO_LARGE: 'PAGE_TOO_LARGE',
  RENDERER_UNAVAILABLE: 'RENDERER_UNAVAILABLE',
  RENDERER_BUSY: 'RENDERER_BUSY',
  RATE_LIMITED: 'RATE_LIMITED',
  INVALID_VIEWPORT: 'INVALID_VIEWPORT',
  INVALID_FORMAT: 'INVALID_FORMAT',
  INVALID_DELAY: 'INVALID_DELAY',
  UNAUTHORIZED: 'UNAUTHORIZED',
  INTERNAL_ERROR: 'INTERNAL_ERROR',
} as const;

export type ErrorCode = (typeof ERROR_CODES)[keyof typeof ERROR_CODES];
