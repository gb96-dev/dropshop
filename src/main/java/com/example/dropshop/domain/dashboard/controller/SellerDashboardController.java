package com.example.dropshop.domain.dashboard.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.dashboard.dto.response.SellerDashboardOrderItemResponse;
import com.example.dropshop.domain.dashboard.dto.response.SellerDashboardSummaryResponse;
import com.example.dropshop.domain.dashboard.service.SellerDashboardQueryService;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매자 대시보드 API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/me/dashboard")
public class SellerDashboardController {

  private final SellerDashboardQueryService sellerDashboardQueryService;
  private final SellerAuthResolver sellerAuthResolver;

  @GetMapping("/summary")
  public ResponseEntity<ApiResponse<SellerDashboardSummaryResponse>> getSummary(
      @AuthenticationPrincipal String email,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    return ResponseEntity.ok(ApiResponse.ok(
        sellerDashboardQueryService.getSummary(sellerAuth.sellerId(), from, to)
    ));
  }

  @GetMapping("/orders")
  public ResponseEntity<ApiResponse<ApiResponse.PageResponse<SellerDashboardOrderItemResponse>>> getOrders(
      @AuthenticationPrincipal String email,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    Pageable pageable = PageRequest.of(page, size);
    Page<SellerDashboardOrderItemResponse> response = sellerDashboardQueryService.getOrderItems(
        sellerAuth.sellerId(),
        status,
        from,
        to,
        pageable
    );
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
