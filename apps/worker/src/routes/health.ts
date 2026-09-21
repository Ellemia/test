import type { Context } from 'hono';
import type { Env } from '../types.js';

// Does NOT ping the renderer on every call (spec: don't health-check renderer
// per-request) — this only reports that the Worker itself is up and configured.
export function healthHandler(c: Context<{ Bindings: Env }>): Response {
  const configured = Boolean(c.env.RENDERER_URL && c.env.RENDERER_SECRET);
  return c.json({ status: 'ok', rendererConfigured: configured });
}
