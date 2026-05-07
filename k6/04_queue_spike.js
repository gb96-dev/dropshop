/**
 * 시나리오 4: 드랍 오픈 순간 대기열 스파이크 테스트
 *
 * 목적: 드랍 오픈 직후 동시 요청이 몰릴 때 대기열 시스템의 처리량 측정
 *       - POST /api/queues?dropId={id}
 *       - Redis 기반 대기열 처리 성능 확인
 *       - 스파이크(급격한 트래픽 증가) 상황 시뮬레이션
 *
 * 주의: DROP_ID, 사용자 수는 실제 환경에 맞게 조정 필요.
 *
 * 실행:
 *   k6 run k6/04_queue_spike.js
 *   k6 run -e DROP_ID=1 k6/04_queue_spike.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';
import { BASE_URL, THRESHOLDS, JSON_HEADERS, authHeader } from './config.js';

const queueDuration = new Trend('queue_duration');
const failRate      = new Rate('queue_fail_rate');
const passCount     = new Counter('queue_pass_count');   // 즉시 입장
const waitCount     = new Counter('queue_wait_count');   // 대기열 진입

export const options = {
  thresholds: {
    ...THRESHOLDS,
    queue_duration: ['p(95)<200'],   // 대기열 판단은 빨라야 함
    queue_fail_rate: ['rate<0.01'],
  },
  scenarios: {
    // 드랍 오픈 순간 스파이크 (로컬 환경: Windows 소켓 제한 고려)
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '5s',  target: 20 }, // 즉시 스파이크
        { duration: '30s', target: 20 }, // 유지
        { duration: '10s', target: 0  }, // 감소
      ],
    },
  },
};

const DROP_ID = parseInt(__ENV.DROP_ID || '1');

// 테스트 유저 목록 (DB 미리 생성 필요)
const TEST_USERS = [
  { email: 'user1@test.com', password: 'password' },
  { email: 'user2@test.com', password: 'password' },
  { email: 'user3@test.com', password: 'password' },
  { email: 'user4@test.com', password: 'password' },
  { email: 'user5@test.com', password: 'password' },
];

// 로그인 토큰 캐시 (setup에서 미리 발급)
export function setup() {
  const tokens = {};
  for (const user of TEST_USERS) {
    const res = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ email: user.email, password: user.password }),
      { headers: JSON_HEADERS }
    );
    if (res.status === 200) {
      try { tokens[user.email] = JSON.parse(res.body).data; } catch {}
    }
  }
  return { tokens };
}

export default function ({ tokens }) {
  const user = TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];
  const token = tokens[user.email];

  if (!token) {
    failRate.add(1);
    return;
  }

  const res = http.post(
    `${BASE_URL}/api/queues?dropId=${DROP_ID}`,
    null,
    { headers: { ...authHeader(token) } }
  );

  queueDuration.add(res.timings.duration);

  const ok = check(res, {
    '대기열 응답 200': (r) => r.status === 200,
  });

  failRate.add(!ok);

  if (ok) {
    try {
      const body = JSON.parse(res.body);
      const result = body.data?.result;
      if (result === 'DIRECT') passCount.add(1);
      if (result === 'QUEUE')  waitCount.add(1);
    } catch {}
  }

  sleep(0.5); // 로컬 Windows 환경에서 소켓 고갈 방지
}
