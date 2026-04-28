package com.example.dropshop.domain.statistics.dto.response;

import java.math.BigDecimal;
import lombok.Getter;

/**
 * 인기 상품 응답 DTO (판매량 기준 랭킹).
 */
@Getter
public class PopularProductResponse {

    private final Long productId;
    private final String productName;
    private final String category;
    private final Long totalQuantity;
    private final BigDecimal totalAmount;

    public PopularProductResponse(Long productId, String productName, String category,
                                   Long totalQuantity, BigDecimal totalAmount) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.totalQuantity = totalQuantity != null ? totalQuantity : 0L;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }
}
