# 🛍️ Dropshop — 한정판 드랍 커머스 플랫폼

한정된 수량의 상품을 특정 시간에 오픈하는 **드랍(Drop)** 방식의 이커머스 백엔드 서버입니다.  
대기열, 실시간 알림, AI 추천 등 고트래픽 상황을 고려한 시스템 설계에 초점을 맞췄습니다.

---

## 📌 주요 기능

| 기능 | 설명 |
|------|------|
| **회원 / 인증** | 회원가입, 로그인, JWT 발급, 토큰 갱신, 로그아웃, 비밀번호 변경 |
| **판매자 관리** | 판매자 신청, 관리자 승인/거절, 판매자 정보 수정 |
| **상품 관리** | 상품 등록·수정·삭제, 이미지 등록, 상태 변경, 버전 관리(낙관적 락) |
| **드랍(Drop)** | 드랍 생성, 상태 전환(UPCOMING → ACTIVE → ENDED), 자동 스케줄러 |
| **대기열** | Redis ZSET 기반 선착순 대기열, admissionToken 발급, 주문 인터셉터 검증 |
| **위시리스트** | 드랍별 위시리스트 등록/삭제, Redis 캐싱, QueryDSL 조회 |
| **SSE 실시간 알림** | 드랍 오픈 시 위시리스트 등록 유저에게 SSE Push 알림 |
| **주문 / 결제** | 주문 생성(HOLD), PortOne 결제 준비·완료, 아웃박스 패턴, 재고 확정 |
| **환불** | 환불 요청, PortOne 취소 API 연동, 재고 복원 |
| **통계 / 대시보드** | 판매자 매출 추이, 카테고리별 매출, 인기 상품(Redis 랭킹) |
| **AI 추천** | 자연어 질의 → OpenAI 임베딩 → Pinecone 벡터 검색 |
| **알림 내역** | 수신 알림 목록 조회, 읽음 처리 |

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.0, Spring Security, Spring Data JPA |
| Database | MySQL 8, Redis |
| Message Broker | Apache Kafka |
| ORM / Query | Hibernate, QueryDSL |
| DB Migration | Flyway |
| Distributed Lock | ShedLock |
| Payment | PortOne (Mock 모드 지원) |
| Storage | AWS S3 (Presigned URL) |
| AI | OpenAI Embeddings API + Pinecone 벡터 DB |
| 인증 | JWT (jjwt 0.12) |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Code Quality | Google Java Format, Checkstyle, Spotless |
| Test | JUnit 5, Postman 통합 테스트 |

---

## 🏗 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                          Client                                  │
│          Browser / Postman / Mobile App                          │
└──────────────────────┬──────────────────────────────────────────┘
                       │ REST API (HTTP/SSE)
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ Controllers  │  │  Security    │  │  Global Exception    │  │
│  │ (REST / SSE) │  │  Filter(JWT) │  │  Handler             │  │
│  └──────┬───────┘  └──────────────┘  └──────────────────────┘  │
│         │                                                        │
│  ┌──────▼───────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │   Services   │  │    Kafka     │  │    SSE Emitter       │  │
│  │  (Facade)    │◄─►│   Producer  │  │    Service           │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────────────────┘  │
│         │                 │                                      │
│  ┌──────▼───────┐         │                                      │
│  │ Repositories │         │                                      │
│  │ (JPA/QueryDSL│         │                                      │
│  └──────┬───────┘         │                                      │
└─────────┼─────────────────┼────────────────────────────────────┘
          │                 │
    ┌─────▼──────┐    ┌─────▼──────────────────────────────────┐
    │   MySQL    │    │              Kafka Topics               │
    │ (영속성)    │    │  wishlist-drop-notification            │
    └────────────┘    │  order-events / drop-status-changed    │
                      │  payment-outbox / user-activity        │
    ┌────────────┐    └─────────────────────────────────────────┘
    │   Redis    │
    │ Queue ZSET │    ┌────────────┐    ┌──────────────────────┐
    │ Wishlist   │    │  PortOne   │    │  OpenAI + Pinecone   │
    │ 인기상품 랭킹│    │  결제 API  │    │  AI 상품 추천        │
    └────────────┘    └────────────┘    └──────────────────────┘
```

---

## ⚡ 핵심 구현 포인트

### 1. 드랍 & 선착순 대기열

드랍 오픈 시 동시 접속자가 몰리는 상황을 Redis ZSET 기반 대기열로 처리합니다.

```
구매자                 Spring Boot              Redis               MySQL
  │                        │                     │                    │
  │  POST /api/queues       │                     │                    │
  │  ?dropId={id}  ────────►│                     │                    │
  │                        │── ZADD queue:{id} ──►│                    │
  │                        │                     │                    │
  │◄─── admissionToken ────│                     │                    │
  │                        │                     │                    │
  │  POST /api/orders  ────►│                     │                    │
  │  (admissionToken 포함)  │── 토큰 검증 인터셉터 ─►│                   │
  │                        │── 재고 임시 차감 ─────────────────────────►│
  │◄─── orderId (HOLD) ────│                     │                    │
```

- **Redis ZSET**: `score = 입장 순서`, 선착순 보장
- **QueueTokenValidationInterceptor**: 주문 API 진입 전 토큰 유효성 검증
- **HOLD 상태**: 주문 생성 후 10분간 재고 임시 확보, 미결제 시 자동 해제

### 2. 위시리스트 → SSE 실시간 드랍 알림

```
구매자 위시리스트 등록
      │
      ▼
Redis ZSET 캐싱 (wishlist:{dropId})
      │
드랍 상태 ACTIVE 변경 (관리자)
      │
      ▼
Kafka 발행: drop-status-changed
      │
      ▼
Kafka Consumer: Redis에서 위시리스트 유저 목록 조회
      │
      ▼
SseEmitter.send() → 각 유저 브라우저에 실시간 Push
```

### 3. 주문 → PortOne 결제 → 아웃박스 패턴

```
[1] POST /api/orders           → Order(HOLD) 생성, 재고 -1 (임시)
[2] POST /api/payments/prepare → Payment(PENDING) 생성, paymentKey 발급
[3] POST /api/payments/complete → PortOne 검증, Payment(DONE), Order(CONFIRMED)
                                   PaymentOutbox → Kafka 발행 (비동기 후처리)
[4] PATCH /api/orders/{id}/cancel → 취소 요청
[5] POST /api/refunds           → PortOne 환불 + 재고 복원
```

- **낙관적 락**: 상품 재고 동시 수정 충돌 방지 (`@Version`)
- **아웃박스 패턴**: 결제 이벤트 유실 없이 Kafka 발행 보장

### 4. AI 상품 추천 (OpenAI + Pinecone)

```
GET /api/recommendations?query=여름에 입기 좋은 캐주얼
      │
      ▼
OpenAI Embeddings API → 질의 벡터화
      │
      ▼
Pinecone 벡터 검색 → 유사 상품 ID 반환
      │
      ▼
추천 상품 목록 응답
```

---

## 📡 API 명세

서버 실행 후 Swagger UI에서 전체 API를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

통합 테스트는 `dropshop-integration-test.postman_collection.json`을 Postman에 임포트하여 실행할 수 있습니다.

---

## 🚀 로컬 실행 방법

### 사전 요구사항

- Java 17
- MySQL 8.0
- Redis 7.x
- Apache Kafka 3.x

### 1. 인프라 실행 순서

```bash
# 1. MySQL 실행
mysql.server start   # macOS
# 또는 net start MySQL80   # Windows

# 2. Redis 실행
redis-server

# 3. Kafka 실행 (Zookeeper 포함)
bin/zookeeper-server-start.sh config/zookeeper.properties &
bin/kafka-server-start.sh config/server.properties &
```

### 2. DB 생성

```sql
CREATE DATABASE dropshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

### 5. AI 추천 기능 활성화 (선택)

`application-local.yml`에 아래 설정 추가:

```yaml
recommendation:
  enabled: true
  openai:
    api-key: "sk-..."
  pinecone:
    api-key: "..."
    index-name: "..."
```

---

## 📂 프로젝트 구조

```
src/main/java/com/example/dropshop/
├── common/
│   ├── config/          # Security, Redis, Kafka, OpenAPI 설정
│   ├── constant/        # Kafka Topic, Group 상수
│   ├── dto/             # ApiResponse 공통 응답
│   ├── exception/       # GlobalExceptionHandler, ErrorCode
│   ├── jwt/             # JwtUtil, JwtAuthenticationFilter
│   ├── kafka/           # EventKafkaProducer
│   └── lock/            # RedisLockService (분산 락)
│
└── domain/
    ├── auth/            # 로그인, JWT, SSE, 토큰 블랙리스트
    ├── user/            # 회원가입, 마이페이지
    ├── seller/          # 판매자 신청·관리
    ├── admin/           # 관리자 판매자 승인
    ├── product/         # 상품 CRUD, 이미지
    ├── notification/
    │   └── drops/       # 드랍 생성·상태관리·스케줄러
    ├── queue/           # 대기열 (Redis ZSET + Kafka)
    ├── wishlist/        # 위시리스트 (Redis 캐시)
    ├── order/           # 주문 생성·조회·취소
    ├── payment/         # PortOne 결제 (아웃박스 패턴)
    ├── refund/          # 환불 처리
    ├── statistics/      # 매출 통계, 인기 상품
    ├── dashboard/       # 판매자 대시보드
    ├── recommendation/  # AI 상품 추천 (OpenAI + Pinecone)
    └── terms/           # 이용약관
```

---

## 🔧 기타 설정

### PortOne Mock 결제

`application-local.yml`에서 `portone.mock: true` 설정 시 실제 결제 없이 테스트 가능합니다.

### 통합 테스트

`dropshop-integration-test.postman_collection.json` 파일을 Postman에 임포트하여  
판매자 플로우(회원가입→로그인→판매자신청→승인→상품등록→드랍생성)와  
구매자 플로우(회원가입→로그인→위시리스트→대기열→주문→결제)를 순서대로 테스트할 수 있습니다.
