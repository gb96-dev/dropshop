# k6 성능 테스트

## 사전 준비

### k6 설치
```bash
# macOS
brew install k6

# Windows (Chocolatey)
choco install k6

# Docker
docker pull grafana/k6
```

### DB 테스트 데이터 생성
아래 유저들이 DB에 존재해야 한다:
- user1@test.com ~ user5@test.com (비밀번호: Password1!)

상품/드랍 데이터도 최소 1개 이상 필요하다.

---

## 시나리오별 실행

### 1. 로그인 부하 테스트
```bash
k6 run k6/01_login.js
```

### 2. 상품 조회 부하 테스트
```bash
k6 run k6/02_product_browse.js
```

### 3. 주문 + 결제 준비 흐름
```bash
k6 run -e DROP_ID=1 -e PRODUCT_ID=1 -e AMOUNT=10000 k6/03_order_payment.js
```

### 4. 드랍 오픈 대기열 스파이크
```bash
k6 run -e DROP_ID=1 k6/04_queue_spike.js
```

---

## 환경변수

| 변수           | 기본값                  | 설명              |
|--------------|----------------------|-----------------|
| BASE_URL     | http://localhost:8080 | 서버 주소           |
| DROP_ID      | 1                    | 테스트 드랍 ID       |
| PRODUCT_ID   | 1                    | 테스트 상품 ID       |
| AMOUNT       | 10000                | 결제 금액 (원)       |

---

## 결과 해석

| 지표                  | 의미                        |
|---------------------|-----------------------------|
| `http_req_duration` | 응답시간. p95 < 500ms 목표   |
| `http_req_failed`   | 실패율. 1% 미만 목표          |
| `vus`               | 동시 접속자 수                |
| `iterations`        | 총 실행 횟수                  |

## 결과 출력 (HTML 리포트)
```bash
k6 run --out json=result.json k6/03_order_payment.js
```
