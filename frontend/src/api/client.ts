import { getSessionId } from './session';
import type { FeedbackType, RecipeDetail, RecommendRequest, RecommendResponse } from './types';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8090';

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'X-Session-Id': getSessionId(),
      ...options.headers,
    },
  });
  if (!res.ok) {
    let message = `요청이 실패했습니다 (${res.status})`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
    } catch {
      // response body wasn't JSON — keep the generic message
    }
    throw new Error(message);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export function recommend(req: RecommendRequest): Promise<RecommendResponse> {
  return request('/api/recommendations', { method: 'POST', body: JSON.stringify(req) });
}

export function sendFeedback(
  recommendationId: number,
  recipeId: number,
  feedback: FeedbackType,
): Promise<void> {
  return request(`/api/recommendations/${recommendationId}/feedback`, {
    method: 'POST',
    body: JSON.stringify({ recipeId, feedback }),
  });
}

export function getRecipe(id: number): Promise<RecipeDetail> {
  return request(`/api/recipes/${id}`);
}

export function listFavorites(): Promise<number[]> {
  return request('/api/favorites');
}

export function addFavorite(recipeId: number): Promise<void> {
  return request(`/api/favorites/${recipeId}`, { method: 'POST' });
}

export function removeFavorite(recipeId: number): Promise<void> {
  return request(`/api/favorites/${recipeId}`, { method: 'DELETE' });
}
