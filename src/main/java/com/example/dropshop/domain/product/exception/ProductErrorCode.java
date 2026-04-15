package com.example.dropshop.domain.product.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ProductErrorCode {
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "INVALID_PRICE", "상품 가격이 0 이하입니다."),
    INVALID_DISCOUNT_RATE(HttpStatus.BAD_REQUEST, "INVALID_DISCOUNT_RATE", "할인율은 0~100 사이여야 합니다."),
    IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "IMAGE_REQUIRED", "이미지는 최소 1개 이상 등록해야 합니다."),
    IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "IMAGE_LIMIT_EXCEEDED", "이미지는 최대 5개까지 등록 가능합니다."),
    THUMBNAIL_REQUIRED(HttpStatus.BAD_REQUEST, "THUMBNAIL_REQUIRED", "대표 이미지(isThumbnail=true)가 정확히 1개여야 합니다."),
    PRODUCT_HIDE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PRODUCT_HIDE_NOT_ALLOWED", "판매 중이거나 품절 상태의 상품은 비공개로 변경할 수 없습니다."),
    SELLER_NOT_VERIFIED(HttpStatus.FORBIDDEN, "SELLER_NOT_VERIFIED", "정산 계좌 인증이 완료되지 않았습니다."),
    SELLER_NOT_APPROVED(HttpStatus.FORBIDDEN, "SELLER_NOT_APPROVED", "판매자 승인 대기 상태입니다."),
    SELLER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "SELLER_ROLE_REQUIRED", "판매자 권한이 필요합니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "요청값 검증에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ProductErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}


