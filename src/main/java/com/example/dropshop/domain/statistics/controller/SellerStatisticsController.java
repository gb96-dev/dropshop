package com.example.dropshop.domain.statistics.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.statistics.dto.response.CategorySalesResponse;
import com.example.dropshop.domain.statistics.dto.response.PopularProductResponse;
import com.example.dropshop.domain.statistics.dto.response.SalesTrendResponse;
import com.example.dropshop.domain.statistics.service.StatisticsService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매자 통계 API.
 * SecurityConfig 에서 /api/sellers/me/statistics/** 는 ROLE_SELLER 만 접근 가능.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/me/statistics")
public class SellerStatisticsController {

    private final StatisticsService statisticsService;
    private final SellerAuthResolver sellerAuthResolver;

    /**
     * 날짜별 판매 추이 조회.
     * GET /api/sellers/me/statistics/trend?from=...&to=...
     */
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<SalesTrendResponse>>> getSalesTrend(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
        List<SalesTrendResponse> result = statisticsService.getSalesTrend(from, to, sellerAuth.sellerId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 카테고리별 판매 조회.
     * GET /api/sellers/me/statistics/categories?from=...&to=...
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategorySalesResponse>>> getCategorySales(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
        List<CategorySalesResponse> result = statisticsService.getCategorySales(from, to, sellerAuth.sellerId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 인기 상품 조회 (판매량 기준).
     * GET /api/sellers/me/statistics/popular-products?from=...&to=...&limit=10
     */
    @GetMapping("/popular-products")
    public ResponseEntity<ApiResponse<List<PopularProductResponse>>> getPopularProducts(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
        List<PopularProductResponse> result = statisticsService.getPopularProducts(from, to, sellerAuth.sellerId(), limit);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
