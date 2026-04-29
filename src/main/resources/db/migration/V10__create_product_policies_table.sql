-- 상품 공통 정책 테이블 생성
CREATE TABLE IF NOT EXISTS product_policies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '정책 ID',
  policy_type VARCHAR(20) NOT NULL COMMENT '정책 유형 (DELIVERY, REFUND)',
  content LONGTEXT NOT NULL COMMENT '정책 내용',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
  CONSTRAINT uk_product_policies_type UNIQUE (policy_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상품 공통 정책 (배송, 환불)';

-- 초기 배송 정책 데이터
INSERT INTO product_policies (policy_type, content)
VALUES (
  'DELIVERY',
  '기본 배송 정책을 입력하세요'
)
ON DUPLICATE KEY UPDATE
  content = content;

-- 초기 환불 정책 데이터
INSERT INTO product_policies (policy_type, content)
VALUES (
  'REFUND',
  '기본 환불 정책을 입력하세요'
)
ON DUPLICATE KEY UPDATE
  content = content;

