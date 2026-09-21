/**
 * Best-effort per-isolate rate limiter. A Worker isolate is not guaranteed to
 * stay warm or to be the one that handles a given client's next request, so
 * this only smooths out abuse within a single isolate's lifetime — it is NOT
 * a global limit. For a real deployment, put a Cloudflare Rate Limiting rule
 * in front of this route (dashboard or `wrangler.toml` -> Rules), or replace
 * this with a Durable Object / KV-backed counter for a real global limit.
 */
const buckets = new Map<string, { count: number; windowStart: number }>();
const WINDOW_MS = 60_000;

export function isRateLimited(clientKey: string, limitPerMinute: number): boolean {
  const now = Date.now();
  const bucket = buckets.get(clientKey);

  if (!bucket || now - bucket.windowStart >= WINDOW_MS) {
    buckets.set(clientKey, { count: 1, windowStart: now });
    return false;
  }

  bucket.count += 1;
  return bucket.count > limitPerMinute;
}
