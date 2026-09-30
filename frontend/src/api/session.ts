const SESSION_KEY = 'moodfood.sessionId';

/** Anonymous per-browser session id (spec: no forced signup). Generated once
 * and persisted in localStorage; sent as X-Session-Id on every API call. */
export function getSessionId(): string {
  let id = localStorage.getItem(SESSION_KEY);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(SESSION_KEY, id);
  }
  return id;
}
