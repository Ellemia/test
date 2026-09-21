# WebShot — URL → Screenshot

A developer tool: enter a URL, pick a device size, and get back a real
Chromium-rendered screenshot (PNG or WebP). Three services:

```
apps/web       Vite + React + TS frontend (Cloudflare Pages)
apps/worker    Cloudflare Worker API — validation, auth to renderer, CORS, rate limit
apps/renderer  Node + Playwright — the only part that actually launches Chromium
packages/shared  Types, zod schemas, constants shared by renderer + worker + web
```

The Worker never launches Chromium itself — it forwards validated requests to
the Renderer over an authenticated HTTP call. See `54. 중요한 설계 원칙` in the
original spec for why: Cloudflare Workers cannot run Playwright/Chromium.

## Status: local MVP, not deployed

Everything below runs and has been verified locally in this session:
SSRF defenses (7 unit tests), real screenshot capture (PNG + WebP, viewport +
full-page, custom + preset viewports), the Worker→Renderer auth handshake,
CORS, basic rate limiting, and the full frontend flow (capture, error states,
download, copy URL) driven end-to-end with a real browser.

**Not done, and not something this session can do:** actual deployment to
Cloudflare Pages/Workers or a production VPS for the renderer — both require
your own Cloudflare account and a server to host the renderer on. See
"Deploying" below for what that involves.

## Local development

Three terminals, in this order:

```bash
# 1. Renderer (needs Playwright's Chromium — already installed if you're in
#    this same sandbox; otherwise `npx playwright install chromium` first)
cd apps/renderer
cp .env.example .env   # set RENDERER_SECRET to a random string
npm run dev            # listens on :3001

# 2. Worker
cd apps/worker
cp .dev.vars.example .dev.vars   # RENDERER_URL=http://localhost:3001, same RENDERER_SECRET
npm run dev            # wrangler dev, listens on :8787

# 3. Frontend
cd apps/web
echo "VITE_API_BASE_URL=http://localhost:8787" > .env.local
npm run dev             # listens on :5173
```

Open `http://localhost:5173`.

From the repo root, `npm install` installs all four workspaces at once
(npm workspaces — see root `package.json`).

### Renderer env vars worth knowing about

`apps/renderer/.env.example` documents these; the last two are **dev-only**:

- `RENDERER_HTTP_PROXY` — only if this renderer must reach the internet
  through an egress proxy. A normal VPS with direct outbound access leaves
  this unset.
- `RENDERER_INSECURE_IGNORE_CERT_ERRORS` — **never set to `true` in
  production.** It disables TLS certificate validation for every target URL.
  It exists only so the renderer can be smoke-tested behind a sandbox's own
  TLS-intercepting dev proxy (which is exactly the situation this was built
  and tested in — see `apps/renderer/.env.example` for details).

## Security model

The renderer visits whatever URL a client asks it to, so SSRF is the main
risk. `apps/renderer/src/security/ssrf.ts` (covered by
`apps/renderer/tests/ssrf.test.ts`, 7 passing tests) blocks:

- non-http(s) protocols (`file:`, `data:`, `javascript:`, …)
- `localhost` / loopback / link-local / RFC1918 private ranges / CGNAT, for
  both IPv4 and IPv6
- DNS rebinding: every hostname is re-resolved and re-checked, and — because
  the initial URL can be safe while a redirect target isn't — **every
  redirect hop is re-validated too**, via a Playwright route handler that
  aborts navigation to any hop that fails the same check.

The Worker never talks to Chromium directly; it authenticates to the
Renderer with a shared bearer secret (`RENDERER_SECRET`) so the Renderer
can't be driven by anyone who doesn't hold that secret.

## What's MVP-only / simplified vs. the original spec

- **WebP**: Playwright's `page.screenshot()` only emits PNG/JPEG natively.
  WebP is produced by capturing PNG and re-encoding with `sharp`.
- **Rate limiting**: `apps/worker/src/security/rateLimit.ts` is an in-memory
  per-isolate counter — good enough to blunt casual abuse locally, but not a
  global limit (a Worker isolate isn't guaranteed to see the same client
  twice). For production, put a Cloudflare Rate Limiting rule in front of
  `/api/capture` (dashboard-configured, no code) or swap this for a Durable
  Object / KV-backed counter.
- **No R2 / job queue / batch / diff / history** — all explicitly out of
  scope for MVP per the spec (`54`, V2/V3 sections). The API shape
  (`{ url, viewport, fullPage, format, delay }`) was kept close to the spec
  so a `POST /api/jobs` async version can be added later without a client
  contract change beyond the response shape.
- **No UI inspection / performance / accessibility analysis** (V3) — not
  built.

## Deploying (not done here — needs your own accounts/infra)

1. **Renderer**: needs a real Linux VM with direct outbound internet access
   (a Playwright/Chromium environment) — a small VPS (Hetzner, Fly.io,
   Railway, Oracle Cloud, …) works. Run it under systemd with
   `Restart=always`, set a strong `RENDERER_SECRET`, and do **not** set
   `RENDERER_INSECURE_IGNORE_CERT_ERRORS`.
2. **Worker**: `wrangler login`, then `wrangler secret put RENDERER_URL` and
   `wrangler secret put RENDERER_SECRET` (pointing at the renderer VM's
   public URL and its secret), then `npm run deploy` from `apps/worker`.
   Set `ALLOWED_ORIGIN` in `wrangler.toml` to the real frontend origin.
3. **Frontend**: `wrangler pages deploy` (or connect the repo in the
   Cloudflare dashboard) from `apps/web`, with `VITE_API_BASE_URL` pointing
   at the deployed Worker.

## Known dev-tooling audit findings

`npm audit` reports vulnerabilities only in dev-time dependency trees
(wrangler's bundled esbuild/undici/ws, sharp's bundled libvips at its
current pinned version) — `npm audit --omit=dev` reports zero. Worth
revisiting (`npm audit fix`) before a production deploy, but doesn't affect
what ships to a browser or runs in the renderer's request path today.
