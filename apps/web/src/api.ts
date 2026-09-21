import type { CaptureRequest, ApiErrorBody } from '@webshot/shared';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8787';

export class CaptureApiError extends Error {
  code: string;
  constructor(code: string, message: string) {
    super(message);
    this.name = 'CaptureApiError';
    this.code = code;
  }
}

export interface CaptureSuccess {
  blob: Blob;
  objectUrl: string;
  width: number;
  height: number;
  format: string;
  bytes: number;
  durationMs: number;
}

export async function captureScreenshot(req: CaptureRequest): Promise<CaptureSuccess> {
  const response = await fetch(`${API_BASE_URL}/api/capture`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  });

  if (!response.ok) {
    let body: ApiErrorBody | null = null;
    try {
      body = await response.json();
    } catch {
      // ignore — fall through to generic error below
    }
    throw new CaptureApiError(
      body?.error.code ?? 'INTERNAL_ERROR',
      body?.error.message ?? 'Screenshot service is temporarily unavailable.',
    );
  }

  const blob = await response.blob();
  const objectUrl = URL.createObjectURL(blob);

  return {
    blob,
    objectUrl,
    width: Number(response.headers.get('x-capture-width') ?? 0),
    height: Number(response.headers.get('x-capture-height') ?? 0),
    format: response.headers.get('content-type')?.includes('webp') ? 'webp' : 'png',
    bytes: blob.size,
    durationMs: Number(response.headers.get('x-capture-duration-ms') ?? 0),
  };
}
