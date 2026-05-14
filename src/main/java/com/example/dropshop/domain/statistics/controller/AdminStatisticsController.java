package com.example.dropshop.domain.statistics.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.statistics.dto.response.CategorySalesResponse;
import com.example.dropshop.domain.statistics.dto.response.PopularProductResponse;
import com.example.dropshop.domain.statistics.dto.response.SalesTrendResponse;
import com.example.dropshop.domain.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 통계 API (전체 집계). SecurityConfig 에서 /api/admin/statistics/** 는 ROLE_ADMIN 만 접근 가능. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/statistics")
@Tag(name = "Admin Statistics", description = "관리자 통계 API")
@SecurityRequirement(name = "bearerAuth")
public class AdminStatisticsController {

  private final StatisticsService statisticsService;

  /** 전체 날짜별 판매 추이 조회. GET /api/admin/statistics/trend?from=...&to=... */
  @GetMapping("/trend")
  @Operation(summary = "전체 판매 추이 조회", description = "전체 서비스의 기간별 판매 추이를 조회합니다.")
  public ResponseEntity<ApiResponse<List<SalesTrendResponse>>> getSalesTrend(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime to) {
    List<SalesTrendResponse> result = statisticsService.getSalesTrend(from, to, null);
    return ResponseEntity.ok(ApiResponse.ok(result));
  }

  /** 전체 카테고리별 판매 조회. GET /api/admin/statistics/categories?from=...&to=... */
  @GetMapping("/categories")
  @Operation(summary = "전체 카테고리 판매 조회", description = "전체 서비스의 카테고리별 매출을 조회합니다.")
  public ResponseEntity<ApiResponse<List<CategorySalesResponse>>> getCategorySales(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime to) {
    List<CategorySalesResponse> result = statisticsService.getCategorySales(from, to, null);
    return ResponseEntity.ok(ApiResponse.ok(result));
  }

  /** 전체 인기 상품 조회 (판매량 기준). GET /api/admin/statistics/popular-products?from=...&to=...&limit=10 */
  @GetMapping("/popular-products")
  @Operation(summary = "전체 인기 상품 조회", description = "전체 서비스의 인기 상품을 판매량 기준으로 조회합니다.")
  public ResponseEntity<ApiResponse<List<PopularProductResponse>>> getPopularProducts(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime to,
      @RequestParam(defaultValue = "10") int limit) {
    List<PopularProductResponse> result =
        statisticsService.getPopularProducts(from, to, null, limit);
    return ResponseEntity.ok(ApiResponse.ok(result));
  }
}
