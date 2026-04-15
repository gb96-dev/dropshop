package com.example.dropshop.domain.order.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.order.dto.response.OrderCreateResponse;
import com.example.dropshop.domain.order.dto.response.OrderGetoneResponse;
import com.example.dropshop.domain.order.service.OrderFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public ResponseEntity<ApiResponse<OrderGetoneResponse>> findOrderById(
      @PathVariable Long orderId) {
    // TODO: @AuthenticationPrincipal로 userId 주입
    Long userId = 1L;
    return ResponseEntity.ok(ApiResponse.ok(orderFacadeService.findOrderById(orderId, userId)));
  }
}