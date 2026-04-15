package com.example.dropshop.domain.drops.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DropsErrorCode {
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "INVALID_DROP_DATE_RANGE", "드랍 종료 시간은 시작 시간보다 뒤여야 합니다."),
    INVALID_TOTAL_STOCK(HttpStatus.BAD_REQUEST, "INVALID_DROP_TOTAL_STOCK", "드랍 총 판매 수량은 0보다 커야 합니다."),
    INVALID_REMAIN_STOCK(HttpStatus.BAD_REQUEST, "INVALID_DROP_REMAIN_STOCK", "잔여 수량은 0 이상이며 총 판매 수량 이하여야 합니다."),
    INVALID_PURCHASE_LIMIT(HttpStatus.BAD_REQUEST, "INVALID_DROP_PURCHASE_LIMIT", "1인당 구매 제한 수량은 1 이상이어야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    DropsErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

