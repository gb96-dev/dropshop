-- products 테이블에 낙관적 락용 version 컬럼 추가
-- 목적: 이미지/상태/가격 동시 수정 시 lost update 방지
-- 기존 데이터는 DEFAULT 0으로 초기화
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0
        COMMENT '낙관적 락 버전 (JPA @Version)';

