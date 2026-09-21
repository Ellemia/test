import { useState } from 'react';
import {
  ALLOWED_DELAYS_MS,
  DEVICE_PRESETS,
  type DevicePresetId,
  type ImageFormat,
} from '@webshot/shared';
import { captureScreenshot, CaptureApiError, type CaptureSuccess } from './api.js';
import { friendlyErrorMessage } from './errorMessages.js';

type CaptureMode = 'viewport' | 'fullPage';

const PRESET_IDS = Object.keys(DEVICE_PRESETS) as DevicePresetId[];

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function App() {
  const [url, setUrl] = useState('');
  const [preset, setPreset] = useState<DevicePresetId | 'custom'>('desktop');
  const [customWidth, setCustomWidth] = useState(1440);
  const [customHeight, setCustomHeight] = useState(900);
  const [customDpr, setCustomDpr] = useState(1);
  const [captureMode, setCaptureMode] = useState<CaptureMode>('viewport');
  const [format, setFormat] = useState<ImageFormat>('png');
  const [delay, setDelay] = useState<number>(500);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<CaptureSuccess | null>(null);
  const [copied, setCopied] = useState(false);

  const viewport =
    preset === 'custom'
      ? { width: customWidth, height: customHeight, deviceScaleFactor: customDpr }
      : {
          width: DEVICE_PRESETS[preset].width,
          height: DEVICE_PRESETS[preset].height,
          deviceScaleFactor: DEVICE_PRESETS[preset].deviceScaleFactor,
        };

  async function handleCapture() {
    setError(null);
    setCopied(false);
    if (result) URL.revokeObjectURL(result.objectUrl);
    setResult(null);

    if (!url.trim()) {
      setError('Please enter a URL.');
      return;
    }

    setLoading(true);
    try {
      const captured = await captureScreenshot({
        url: url.trim(),
        viewport,
        fullPage: captureMode === 'fullPage',
        format,
        delay,
      });
      setResult(captured);
    } catch (err) {
      if (err instanceof CaptureApiError) {
        setError(friendlyErrorMessage(err.code, err.message));
      } else {
        setError('Screenshot service is temporarily unavailable.');
      }
    } finally {
      setLoading(false);
    }
  }

  function handleDownload() {
    if (!result) return;
    const a = document.createElement('a');
    a.href = result.objectUrl;
    a.download = `webshot.${result.format}`;
    a.click();
  }

  async function handleCopyUrl() {
    if (!result) return;
    try {
      await navigator.clipboard.writeText(result.objectUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // clipboard API can be unavailable (e.g. insecure context) — silently ignore
    }
  }

  return (
    <div className="page">
      <header className="hero">
        <h1>WebShot</h1>
        <p>Capture any webpage.</p>
      </header>

      <main className="panel">
        <label className="field">
          <span className="field-label">URL</span>
          <input
            type="text"
            className="url-input"
            placeholder="https://example.com"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleCapture();
            }}
          />
        </label>

        <div className="field">
          <span className="field-label">Device</span>
          <div className="button-row">
            {PRESET_IDS.map((id) => (
              <button
                key={id}
                type="button"
                className={`chip ${preset === id ? 'chip-active' : ''}`}
                onClick={() => setPreset(id)}
              >
                {DEVICE_PRESETS[id].label}
              </button>
            ))}
            <button
              type="button"
              className={`chip ${preset === 'custom' ? 'chip-active' : ''}`}
              onClick={() => setPreset('custom')}
            >
              Custom
            </button>
          </div>
          {preset === 'custom' && (
            <div className="custom-viewport-row">
              <label>
                Width
                <input
                  type="number"
                  value={customWidth}
                  min={320}
                  max={3840}
                  onChange={(e) => setCustomWidth(Number(e.target.value))}
                />
              </label>
              <label>
                Height
                <input
                  type="number"
                  value={customHeight}
                  min={320}
                  max={10000}
                  onChange={(e) => setCustomHeight(Number(e.target.value))}
                />
              </label>
              <label>
                DPR
                <input
                  type="number"
                  value={customDpr}
                  min={1}
                  max={3}
                  step={0.5}
                  onChange={(e) => setCustomDpr(Number(e.target.value))}
                />
              </label>
            </div>
          )}
        </div>

        <div className="field">
          <span className="field-label">Capture</span>
          <div className="button-row">
            <button
              type="button"
              className={`chip ${captureMode === 'viewport' ? 'chip-active' : ''}`}
              onClick={() => setCaptureMode('viewport')}
            >
              Viewport
            </button>
            <button
              type="button"
              className={`chip ${captureMode === 'fullPage' ? 'chip-active' : ''}`}
              onClick={() => setCaptureMode('fullPage')}
            >
              Full Page
            </button>
          </div>
        </div>

        <div className="field-row">
          <label className="field">
            <span className="field-label">Format</span>
            <select value={format} onChange={(e) => setFormat(e.target.value as ImageFormat)}>
              <option value="png">PNG</option>
              <option value="webp">WebP</option>
            </select>
          </label>
          <label className="field">
            <span className="field-label">Delay</span>
            <select value={delay} onChange={(e) => setDelay(Number(e.target.value))}>
              {ALLOWED_DELAYS_MS.map((ms) => (
                <option key={ms} value={ms}>
                  {ms}ms
                </option>
              ))}
            </select>
          </label>
        </div>

        <button type="button" className="capture-btn" onClick={handleCapture} disabled={loading}>
          {loading ? 'Rendering screenshot…' : 'Capture'}
        </button>

        <section className="preview-section">
          <h2>Preview</h2>

          {error && (
            <div className="error-box">
              <p>{error}</p>
              <button type="button" className="btn" onClick={handleCapture}>
                Try Again
              </button>
            </div>
          )}

          {!error && !result && !loading && (
            <div className="preview-placeholder">Your screenshot will appear here.</div>
          )}

          {loading && <div className="preview-placeholder">Rendering screenshot…</div>}

          {result && !loading && (
            <>
              <div className="preview-frame">
                <img src={result.objectUrl} alt="Screenshot preview" />
              </div>
              <div className="preview-meta">
                {result.width} × {result.height} · {result.format.toUpperCase()} · {formatBytes(result.bytes)}
              </div>
              <div className="button-row">
                <button type="button" className="btn primary" onClick={handleDownload}>
                  Download
                </button>
                <button type="button" className="btn" onClick={handleCopyUrl}>
                  {copied ? 'Copied!' : 'Copy URL'}
                </button>
              </div>
            </>
          )}
        </section>
      </main>
    </div>
  );
}
