const FRIENDLY_MESSAGES: Record<string, string> = {
  SSRF_BLOCKED: 'This URL cannot be accessed for security reasons.',
  UNSUPPORTED_PROTOCOL: 'Only http:// and https:// URLs are supported.',
  INVALID_URL: 'Please enter a valid URL.',
  DNS_RESOLUTION_FAILED: 'This domain could not be resolved.',
  TARGET_UNREACHABLE: 'The target site could not be reached.',
  PAGE_TIMEOUT: 'The target server did not respond within the allowed time.',
  SCREENSHOT_TIMEOUT: 'Taking the screenshot took too long. Try again.',
  PAGE_TOO_LARGE: 'This page is too long to capture in full.',
  RENDERER_UNAVAILABLE: 'Screenshot service is temporarily unavailable.',
  RENDERER_BUSY: 'The screenshot service is busy right now. Try again shortly.',
  RATE_LIMITED: "You're sending requests too quickly. Please wait a moment.",
  INTERNAL_ERROR: 'Screenshot service is temporarily unavailable.',
};

export function friendlyErrorMessage(code: string, fallback: string): string {
  return FRIENDLY_MESSAGES[code] ?? fallback;
}
