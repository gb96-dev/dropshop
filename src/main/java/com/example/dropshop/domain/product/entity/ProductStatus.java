package com.example.dropshop.domain.product.entity;

import lombok.Getter;

@Getter
public enum ProductStatus {
    READY("READY", "판매 준비"),
    ON_SALE("ON_SALE", "판매 중"),
    OUT_OF_STOCK("OUT_OF_STOCK", "품절"),
    HIDDEN("HIDDEN", "숨김");

    private final String code;
    private final String description;
    ProductStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
