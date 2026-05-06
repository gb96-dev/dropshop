/**
 * 시나리오 3: 주문 + 결제 준비 흐름 부하 테스트
 *
 * 목적: 로그인 → 주문 생성 → 결제 준비 전체 흐름의 처리량 및 병목 구간 파악
 *       - POST /api/auth/login
 *       - POST /api/orders
 *       - POST /api/payments/prepare
 *       - Redis 분산락, DB 트랜잭션 성능 확인
 *
 * 주의: DB에 실제 상품/드랍 데이터가 있어야 주문이 성공한다.
 *       DROP_ID, PRODUCT_ID, AMOUNT는 환경에 맞게 교체 필요.
 *
 * 실행:
 *   k6 run k6/03_order_payment.js
 *   k6 run -e BASE_URL=http://localhost:8080 k6/03_order_payment.js
 */

import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';
import { BASE_URL, THRESHOLDS, JSON_HEADERS, authHeader, login } from './config.js';

const loginDuration   = new Trend('order_flow_login_duration');
const orderDuration   = new Trend('order_create_duration');
const paymentDuration = new Trend('payment_prepare_duration');
const failRate        = new Rate('order_flow_fail_rate');
const orderCount      = new Counter('order_created_count');

export const options = {
  thresholds: {
    ...THRESHOLDS,
    order_create_duration:   ['p(95)<1000'],  // 주문 생성 (락 + DB)
    payment_prepare_duration: ['p(95)<1000'],
    order_flow_fail_rate:    ['rate<0.05'],   // 동시 주문 충돌 감안해 5%
  },
  scenarios: {
    order_flow: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 10 },
        { duration: '1m',  target: 30 },
        { duration: '20s', target: 0  },
      ],
    },
  },
};

// 테스트 유저 (DB 미리 생성 필요)
const TEST_USERS = [
  { email: 'user1@test.com', password: 'password' },
  { email: 'user2@test.com', password: 'password' },
  { email: 'user3@test.com', password: 'password' },
];

// 테스트 드랍/상품 설정 (실제 DB 값으로 교체 필요)
const DROP_ID    = parseInt(__ENV.DROP_ID    || '1');
const PRODUCT_ID = parseInt(__ENV.PRODUCT_ID || '1');
const AMOUNT     = parseFloat(__ENV.AMOUNT   || '10000');

if (isNaN(DROP_ID)    || DROP_ID    <= 0) throw new Error(`유효하지 않은 DROP_ID: ${__ENV.DROP_ID}`);
if (isNaN(PRODUCT_ID) || PRODUCT_ID <= 0) throw new Error(`유효하지 않은 PRODUCT_ID: ${__ENV.PRODUCT_ID}`);
if (isNaN(AMOUNT)     || AMOUNT     <= 0) throw new Error(`유효하지 않은 AMOUNT: ${__ENV.AMOUNT}`);

export default function () {
  const user = TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];

  // 1) 로그인
  let token;
  group('로그인', () => {
    const res = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ email: user.email, password: user.password }),
      { headers: JSON_HEADERS }
    );
    loginDuration.add(res.timings.duration);
    check(res, { '로그인 성공': (r) => r.status === 200 });
    try { token = JSON.parse(res.body).data; } catch { token = null; }
  });

  if (!token) {
    failRate.add(1);
    return;
  }

  sleep(0.3);

  // 2) 주문 생성
  let orderId;
  group('주문 생성', () => {
    const merchantOrderId = `test-order-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
    const res = http.post(
      `${BASE_URL}/api/orders`,
      JSON.stringify({
        dropId: DROP_ID,
        productId: PRODUCT_ID,
        queueToken: 'test-queue-token',
      }),
      { headers: { ...JSON_HEADERS, ...authHeader(token) } }
    );
    orderDuration.add(res.timings.duration);

    const ok = check(res, {
      '주문 생성 201': (r) => r.status === 201,
    });
    failRate.add(!ok);

    if (ok) {
      orderCount.add(1);
      try {
        orderId = JSON.parse(res.body).data?.orderId;
      } catch {
        orderId = null;
        failRate.add(1);
      }
    }
  });

  if (!orderId) {
    failRate.add(1);
    sleep(1);
    return;
  }

  sleep(0.3);

  // 3) 결제 준비
  group('결제 준비', () => {
    const merchantPaymentId = `test-pay-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
    const res = http.post(
      `${BASE_URL}/api/payments/prepare`,
      JSON.stringify({
        orderId,
        amount: AMOUNT,
        merchantPaymentId,
        paymentMethod: 'CARD',
      }),
      { headers: { ...JSON_HEADERS, ...authHeader(token) } }
    );
    paymentDuration.add(res.timings.duration);

    const ok = check(res, {
      '결제 준비 201': (r) => r.status === 201,
    });
    failRate.add(!ok);
  });

  sleep(1);
}
