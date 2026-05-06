/**
 * 시나리오 2: 상품 조회 부하 테스트
 *
 * 목적: 상품 목록 / 상세 조회 API 처리량 측정
 *       - GET /api/products        (상품 목록, 인증 불필요)
 *       - GET /api/products/{id}   (상품 상세, 인증 불필요)
 *       - 캐시(Redis) 적용 여부에 따른 응답시간 차이 확인
 *
 * 실행:
 *   k6 run k6/02_product_browse.js
 */

import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { BASE_URL, THRESHOLDS } from './config.js';

const listDuration   = new Trend('product_list_duration');
const detailDuration = new Trend('product_detail_duration');
const failRate       = new Rate('product_fail_rate');

export const options = {
  thresholds: {
    ...THRESHOLDS,
    product_list_duration:   ['p(95)<300'],
    product_detail_duration: ['p(95)<300'],
    product_fail_rate:       ['rate<0.01'],
  },
  scenarios: {
    browse: {
      executor: 'constant-vus',
      vus: 50,
      duration: '2m',
    },
  },
};

// 테스트용 상품 ID 목록 (DB에 존재하는 ID로 교체 필요)
const PRODUCT_IDS = [1, 2, 3, 4, 5];

export default function () {
  group('상품 목록 조회', () => {
    const res = http.get(`${BASE_URL}/api/products?page=0&size=20`);
    listDuration.add(res.timings.duration);

    const ok = check(res, {
      '상품 목록 200': (r) => r.status === 200,
      '상품 데이터 존재': (r) => {
        try { return JSON.parse(r.body).data !== null; } catch { return false; }
      },
    });
    failRate.add(!ok);
  });

  sleep(0.5);

  group('상품 상세 조회', () => {
    const id = PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];
    const res = http.get(`${BASE_URL}/api/products/${id}`);
    detailDuration.add(res.timings.duration);

    const ok = check(res, {
      '상품 상세 200': (r) => r.status === 200 || r.status === 404,
    });
    failRate.add(!ok);
  });

  sleep(1);
}
