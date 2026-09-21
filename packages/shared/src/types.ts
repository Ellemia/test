import type { ErrorCode } from './constants.js';

export interface RenderResultMeta {
  width: number;
  height: number;
  format: 'png' | 'webp';
  bytes: number;
  durationMs: number;
}

export interface ApiErrorBody {
  error: {
    code: ErrorCode | string;
    message: string;
  };
}

export interface HealthResponse {
  status: 'ok';
}
