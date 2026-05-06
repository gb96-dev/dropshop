/**
 * k6 공통 설정
 *
 * 사용법:
 *   import { BASE_URL, THRESHOLDS, authHeader } from './config.js';
 */

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

/**
 * 공통 성능 임계값
 * - http_req_duration p95 < 500ms
 * - http_req_failed    < 1%
 */
export const THRESHOLDS = {
  http_req_duration: ['p(95)<500'],
  http_req_failed: ['rate<0.01'],
};

/**
 * 로그인 후 받은 accessToken으로 Authorization 헤더를 생성한다.
 */
export function authHeader(token) {
  return { Authorization: `Bearer ${token}` };
}

/**
 * JSON Content-Type 헤더
 */
export const JSON_HEADERS = { 'Content-Type': 'application/json' };

/**
 * 로그인 요청을 수행하고 accessToken을 반환한다.
 * 실패 시 null 반환.
 */
export function login(http, email, password) {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers: JSON_HEADERS }
  );
  if (res.status !== 200) return null;
  try {
    return JSON.parse(res.body).data; // ApiResponse.data = accessToken
  } catch {
    return null;
  }
}
