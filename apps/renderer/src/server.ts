import express from 'express';
import { healthHandler } from './routes/health.js';
import { renderHandler } from './routes/render.js';
import { getBrowser } from './browser/pool.js';

const PORT = Number(process.env.PORT ?? 3001);
const RENDERER_SECRET = process.env.RENDERER_SECRET;

if (!RENDERER_SECRET) {
  console.error('RENDERER_SECRET env var is required.');
  process.exit(1);
}

const app = express();
app.use(express.json({ limit: '256kb' }));

app.get('/health', healthHandler);

app.post('/render', (req, res, next) => {
  const auth = req.header('authorization');
  if (auth !== `Bearer ${RENDERER_SECRET}`) {
    res.status(401).json({ error: { code: 'UNAUTHORIZED', message: 'Missing or invalid renderer secret.' } });
    return;
  }
  next();
}, (req, res) => {
  renderHandler(req, res).catch((err) => {
    console.error('Unhandled render error', err);
    if (!res.headersSent) {
      res.status(500).json({ error: { code: 'INTERNAL_ERROR', message: 'Screenshot service is temporarily unavailable.' } });
    }
  });
});

async function startupCheck(): Promise<void> {
  const browser = await getBrowser();
  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto('data:text/html,<h1>webshot-startup-check</h1>');
  await context.close();
}

startupCheck()
  .then(() => {
    app.listen(PORT, () => {
      console.log(`webshot renderer listening on :${PORT}`);
    });
  })
  .catch((err) => {
    console.error('Renderer startup check failed — Chromium may not be usable.', err);
    process.exit(1);
  });

process.on('SIGTERM', () => process.exit(0));
process.on('SIGINT', () => process.exit(0));
