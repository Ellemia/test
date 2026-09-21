import { Hono } from 'hono';
import { cors } from 'hono/cors';
import type { Env } from './types.js';
import { captureHandler } from './routes/capture.js';
import { healthHandler } from './routes/health.js';

const app = new Hono<{ Bindings: Env }>();

app.use('*', async (c, next) => {
  const corsMiddleware = cors({
    origin: c.env.ALLOWED_ORIGIN || 'http://localhost:5173',
    allowMethods: ['GET', 'POST', 'OPTIONS'],
    // Without this, the browser's fetch() hides these custom response headers
    // even though they're present on the wire — the frontend needs them to
    // show image dimensions without decoding the image itself.
    exposeHeaders: ['X-Capture-Width', 'X-Capture-Height', 'X-Capture-Duration-Ms'],
  });
  return corsMiddleware(c, next);
});

app.use('*', async (c, next) => {
  await next();
  c.res.headers.set('X-Content-Type-Options', 'nosniff');
  c.res.headers.set('Referrer-Policy', 'no-referrer');
});

app.get('/api/health', healthHandler);
app.post('/api/capture', captureHandler);

app.notFound((c) => c.json({ error: { code: 'NOT_FOUND', message: 'Not found.' } }, 404));

app.onError((err, c) => {
  console.error('Unhandled worker error', err);
  return c.json({ error: { code: 'INTERNAL_ERROR', message: 'Something went wrong.' } }, 500);
});

export default app;
