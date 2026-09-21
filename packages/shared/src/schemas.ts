import { z } from 'zod';
import {
  ALLOWED_DELAYS_MS,
  ALLOWED_FORMATS,
  MAX_DELAY_MS,
  VIEWPORT_LIMITS,
} from './constants.js';

export const viewportSchema = z.object({
  width: z.number().int().min(VIEWPORT_LIMITS.minWidth).max(VIEWPORT_LIMITS.maxWidth),
  height: z.number().int().min(VIEWPORT_LIMITS.minHeight).max(VIEWPORT_LIMITS.maxHeight),
  deviceScaleFactor: z
    .number()
    .min(VIEWPORT_LIMITS.minDeviceScaleFactor)
    .max(VIEWPORT_LIMITS.maxDeviceScaleFactor)
    .default(1),
});

export const captureRequestSchema = z.object({
  url: z.string().url(),
  viewport: viewportSchema,
  fullPage: z.boolean().default(false),
  format: z.enum(ALLOWED_FORMATS).default('png'),
  delay: z
    .number()
    .int()
    .min(0)
    .max(MAX_DELAY_MS)
    .default(0)
    .refine((v) => (ALLOWED_DELAYS_MS as readonly number[]).includes(v), {
      message: `delay must be one of ${ALLOWED_DELAYS_MS.join(', ')}`,
    }),
});

export type CaptureRequest = z.infer<typeof captureRequestSchema>;
export type Viewport = z.infer<typeof viewportSchema>;

export const apiErrorSchema = z.object({
  error: z.object({
    code: z.string(),
    message: z.string(),
  }),
});

export type ApiError = z.infer<typeof apiErrorSchema>;
