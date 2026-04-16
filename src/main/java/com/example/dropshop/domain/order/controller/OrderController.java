package com.example.dropshop.domain.order.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.order.dto.response.OrderCreateResponse;
import com.example.dropshop.domain.order.dto.response.OrderDetailResponse;
import com.example.dropshop.domain.order.dto.response.OrderListItemResponse;
import com.example.dropshop.domain.order.facade.OrderFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 컨트롤러.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderFacadeService orderFacadeService;

  /**
   * 주문 생성.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
      @RequestBody @Valid OrderCreateRequest request) {
    // TODO: @AuthenticationPrincipal로 userId 주입
    Long userId = 1L;
    return ResponseEntity.status(201)
        .body(ApiResponse.created(orderFacadeService.createOrder(userId, request)));
  }

  /**
   * 주문 단건 조회.
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<ApiResponse<OrderDetailResponse>> findOrderById(
      @PathVariable Long orderId) {
    // TODO: @AuthenticationPrincipal로 userId 주입
    Long userId = 1L;
    return ResponseEntity.ok(ApiResponse.ok(orderFacadeService.findOrderById(orderId, userId)));
  }

  /**
   * 주문 목록 조회.
   */
  @GetMapping
  public ResponseEntity<ApiResponse<ApiResponse.PageResponse<OrderListItemResponse>>> findOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Long userId = 1L;
    Pageable pageable = PageRequest.of(page, size);

    Page<OrderListItemResponse> response =
        orderFacadeService.findOrdersByUserId(userId, pageable);

    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /**
   * 주문 수동 취소.
   */
  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
      @PathVariable Long orderId) {
    Long userId = 1L;
    return ResponseEntity.ok(ApiResponse.ok(orderFacadeService.cancelOrder(orderId, userId)));
  }
}