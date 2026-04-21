# Payment Webhook Guide

## Overview
이 문서는 `PortOne` 결제 웹훅 연동과 현재 `payment` 도메인 처리 방식을 정리한다.

현재 결제 흐름은 두 경로를 함께 사용한다.

1. 프론트 리다이렉트 후 `confirm` API를 호출해 결제를 확정한다.
2. PortOne 웹훅이 서버로 직접 들어오면 서버가 결제 상태를 동기화한다.

즉, 사용자가 정상적으로 복귀한 경우에는 `confirm`이 먼저 처리할 수 있고, 프론트 복귀가 누락되거나 지연된 경우에는 웹훅이 상태를 보정하는 구조다.

## Related Files
- [PaymentController.java](/C:/Users/kangd/javasc/dropshop/src/main/java/com/example/dropshop/domain/payment/controller/PaymentController.java)
- [PaymentFacadeService.java](/C:/Users/kangd/javasc/dropshop/src/main/java/com/example/dropshop/domain/payment/facade/PaymentFacadeService.java)
- [PaymentService.java](/C:/Users/kangd/javasc/dropshop/src/main/java/com/example/dropshop/domain/payment/service/PaymentService.java)
- [PaymentWebhookRequest.java](/C:/Users/kangd/javasc/dropshop/src/main/java/com/example/dropshop/domain/payment/dto/request/PaymentWebhookRequest.java)
- [PortOneClient.java](/C:/Users/kangd/javasc/dropshop/src/main/java/com/example/dropshop/domain/payment/client/PortOneClient.java)

## Endpoints
### 1. Prepare payment
- Method: `POST`
- Path: `/api/payments/prepare`
- Purpose: 내부 결제 엔티티를 `PENDING` 상태로 생성한다.

### 2. Get PortOne request payload
- Method: `GET`
- Path: `/api/payments/{paymentId}/portone-request`
- Purpose: 프론트가 PortOne 결제창을 띄우는 데 필요한 값을 조회한다.

### 3. Confirm payment
- Method: `POST`
- Path: `/api/payments/{paymentId}/confirm`
- Purpose: 프론트 복귀 후 PortOne 결제 상태를 서버가 재조회하고 결제를 확정한다.

### 4. Webhook
- Method: `POST`
- Path: `/api/payments/webhook`
- Purpose: PortOne이 비동기로 전달한 결제 이벤트를 기준으로 내부 결제 상태를 동기화한다.

## Webhook Request Handling
웹훅 요청 본문은 [PaymentWebhookRequest.java](/C:/Users/kangd/javasc/dropshop/src/main/java/com/example/dropshop/domain/payment/dto/request/PaymentWebhookRequest.java) 에서 처리한다.

현재 지원하는 결제 식별자 위치는 아래와 같다.

- `id`
- `paymentId`
- `data.id`
- `data.paymentId`

웹훅 바디에서 위 값 중 먼저 찾은 값을 PortOne 결제 식별자로 사용한다.

## Processing Flow
### A. Confirm flow
1. 클라이언트가 `paymentId`와 `portOnePaymentId`로 `confirm` API를 호출한다.
2. 서버는 내부 `Payment`와 `Order`를 조회한다.
3. 서버는 아래 조건을 검증한다.
- 결제가 `PENDING` 상태인지
- 주문이 `PENDING` 상태인지
- 주문 홀드가 만료되지 않았는지
- 내부 `idempotencyKey`와 요청 `portOnePaymentId`가 일치하는지
4. 서버는 PortOne 조회 API로 실제 결제 상태를 다시 확인한다.
5. 상태에 따라 `Payment`와 `Order`를 갱신한다.

### B. Webhook flow
1. PortOne이 `/api/payments/webhook` 으로 이벤트를 전송한다.
2. 서버는 웹훅 본문에서 PortOne 결제 식별자를 추출한다.
3. 내부 `Payment`를 `idempotencyKey` 기준으로 찾는다.
4. 연결된 `Order`를 조회한다.
5. 서버는 PortOne 조회 API로 실제 결제 상태를 다시 확인한다.
6. 상태에 따라 `Payment`와 `Order`를 갱신한다.

웹훅도 최종 판단은 PortOne 조회 API 결과를 기준으로 한다. 즉, 웹훅 바디 자체를 신뢰하기보다 PortOne 서버에 재조회하는 방식이다.

## State Transition
### Success
PortOne 상태가 `PAID` 인 경우:

- `Payment`: `PENDING -> COMPLETED`
- `Order`: `PENDING -> PAID`

추가 동작:
- `transactionId` 저장
- `paidAt` 기록

### Failure
PortOne 상태가 `FAILED` 또는 `CANCELLED` 인 경우:

- `Payment`: `PENDING -> FAILED`
- `Order`: `PENDING -> CANCELLED`

추가 동작:
- 주문 아이템 기준 재고 복원 이벤트 발행

## Idempotency
웹훅은 같은 이벤트가 여러 번 들어올 수 있으므로 멱등성이 중요하다.

현재 구현은 아래 방식으로 중복 처리에 대응한다.

- 내부 `Payment`는 `idempotencyKey`로 조회한다.
- 이미 `Payment` 상태가 `PENDING`이 아니면 추가 상태 전이를 하지 않는다.
- 이미 주문이 `PENDING`이 아니면 취소나 재고 복원도 다시 하지 않는다.

즉, 동일 웹훅이 재수신되어도 결제/주문 상태를 한 번 더 깨지 않도록 처리한다.

## Validation Rules
PortOne 응답에 대해 아래를 검증한다.

- 응답 객체가 존재하는지
- PortOne 결제 ID와 내부 `idempotencyKey`가 같은지
- PortOne 금액 정보가 존재하는지
- PortOne 결제 금액과 내부 결제 금액이 같은지
- PortOne 상태 값이 비어 있지 않은지

추가로 `confirm` 경로에서는 아래도 검증한다.

- 주문 상태가 `PENDING`인지
- 주문 홀드 시간이 만료되지 않았는지

## Why webhook is needed
웹훅이 필요한 이유는 아래와 같다.

- 사용자가 결제 후 브라우저를 닫아도 서버가 결제 완료를 반영할 수 있다.
- 프론트 리다이렉트나 `confirm` 호출이 실패해도 상태를 보정할 수 있다.
- 결제 상태를 프론트 의존 없이 서버 기준으로 동기화할 수 있다.

## Current Limitations
현재 구현에는 아래 한계가 있다.

- PortOne 웹훅 서명 검증은 아직 없다.
- 이벤트 타입별 분기 처리는 따로 없고, 결제 식별자 기반 재조회 방식이다.
- 웹훅 수신 이력 저장 테이블은 없다.
- 실패 상태는 `FAILED`, `CANCELLED` 문자열 비교로 처리한다.

## Recommended Next Steps
- PortOne 웹훅 서명 검증 추가
- 웹훅 원본 payload와 처리 결과 로깅
- 웹훅 중복 수신 이력 저장
- PortOne 상태값 enum 분리
- 운영 환경용 웹훅 재시도/모니터링 정책 정리

## Test Coverage
관련 테스트는 아래 파일에 있다.

- [PaymentServiceTest.java](/C:/Users/kangd/javasc/dropshop/src/test/java/com/example/dropshop/domain/payment/service/PaymentServiceTest.java)
- [PaymentControllerTest.java](/C:/Users/kangd/javasc/dropshop/src/test/java/com/example/dropshop/domain/payment/controller/PaymentControllerTest.java)

주요 검증 포인트:
- 웹훅 성공 처리
- 웹훅 재수신 시 멱등 처리
- 기존 `confirm` 흐름과 같은 검증 규칙 사용
