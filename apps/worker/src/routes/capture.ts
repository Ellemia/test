import type { Context } from 'hono';
import { captureRequestSchema, ERROR_CODES, type ApiErrorBody } from '@webshot/shared';
import type { Env } from '../types.js';
import { isRateLimited } from '../security/rateLimit.js';

function errorBody(code: string, message: string): ApiErrorBody {
  return { error: { code, message } };
}

export async function captureHandler(c: Context<{ Bindings: Env }>): Promise<Response> {
  const clientIp = c.req.header('CF-Connecting-IP') ?? 'unknown';
  const limit = Number(c.env.RATE_LIMIT_PER_MINUTE || '10');
  if (isRateLimited(clientIp, limit)) {
    return c.json(errorBody(ERROR_CODES.RATE_LIMITED, 'Too many requests. Please slow down.'), 429);
  }

  let body: unknown;
  try {
    body = await c.req.json();
  } catch {
    return c.json(errorBody(ERROR_CODES.INVALID_URL, 'Request body must be valid JSON.'), 400);
  }

  const parsed = captureRequestSchema.safeParse(body);
  if (!parsed.success) {
    return c.json(errorBody(ERROR_CODES.INVALID_URL, parsed.error.issues[0]?.message ?? 'Invalid request.'), 400);
  }

  if (!c.env.RENDERER_URL || !c.env.RENDERER_SECRET) {
    return c.json(errorBody(ERROR_CODES.RENDERER_UNAVAILABLE, 'Screenshot service is not configured.'), 503);
  }

  let rendererResponse: Response;
  try {
    rendererResponse = await fetch(`${c.env.RENDERER_URL}/render`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${c.env.RENDERER_SECRET}`,
      },
      body: JSON.stringify(parsed.data),
    });
  } catch {
    return c.json(errorBody(ERROR_CODES.RENDERER_UNAVAILABLE, 'Screenshot service is temporarily unavailable.'), 503);
  }

  if (!rendererResponse.ok) {
    // Renderer already returns our standardized { error: { code, message } } shape.
    const errorPayload = await rendererResponse.json().catch(() => null);
    const payload = errorPayload ?? errorBody(ERROR_CODES.INTERNAL_ERROR, 'Screenshot service is temporarily unavailable.');
    return new Response(JSON.stringify(payload), {
      status: errorPayload ? rendererResponse.status : 502,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  const contentType = rendererResponse.headers.get('content-type') ?? 'image/png';
  const width = rendererResponse.headers.get('x-capture-width');
  const height = rendererResponse.headers.get('x-capture-height');
  const durationMs = rendererResponse.headers.get('x-capture-duration-ms');

  const headers = new Headers();
  headers.set('Content-Type', contentType);
  headers.set('Cache-Control', 'no-store');
  headers.set('X-Content-Type-Options', 'nosniff');
  headers.set('Referrer-Policy', 'no-referrer');
  if (width) headers.set('X-Capture-Width', width);
  if (height) headers.set('X-Capture-Height', height);
  if (durationMs) headers.set('X-Capture-Duration-Ms', durationMs);

  return new Response(rendererResponse.body, { status: 200, headers });
}
