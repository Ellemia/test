import type { Request, Response } from 'express';
import type { HealthResponse } from '@webshot/shared';

export function healthHandler(_req: Request, res: Response<HealthResponse>): void {
  res.json({ status: 'ok' });
}
