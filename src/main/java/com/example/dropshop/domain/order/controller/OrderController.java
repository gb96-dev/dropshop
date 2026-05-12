package com.example.dropshop.domain.order.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.order.dto.response.OrderCreateResponse;
import com.example.dropshop.domain.order.dto.response.OrderDetailResponse;
import com.example.dropshop.domain.order.dto.response.OrderListItemResponse;
import com.example.dropshop.domain.order.facade.OrderFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 주문 컨트롤러. */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 API")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

  private final OrderFacadeService orderFacadeService;

  /**
   * 주문 생성.
   *
   * @param email 인증된 사용자 이메일
   * @param request 주문 생성 요청
   * @return 생성된 주문 응답
   */
  @PostMapping
  @Operation(summary = "주문 생성", description = "드랍과 상품 정보를 기준으로 주문을 생성합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "주문 생성 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 또는 주문 불가 상태",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
      @AuthenticationPrincipal String email, @RequestBody @Valid OrderCreateRequest request) {

    OrderCreateResponse response = orderFacadeService.createOrder(email, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  /**
   * 주문 단건 조회.
   *
   * @param email 인증된 사용자 이메일
   * @param orderId 주문 ID
   * @return 주문 상세 응답
   */
  @GetMapping("/{orderId}")
  @Operation(summary = "주문 단건 조회", description = "주문 ID로 내 주문 상세를 조회합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "주문 조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "주문 없음",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<OrderDetailResponse>> findOrderById(
      @AuthenticationPrincipal String email, @PathVariable Long orderId) {
    return ResponseEntity.ok(ApiResponse.ok(orderFacadeService.findOrderById(orderId, email)));
  }

  /**
   * 주문 목록 조회.
   *
   * @param email 인증된 사용자 이메일
   * @param page 페이지 번호
   * @param size 페이지 크기
   * @return 주문 목록 응답
   */
  @GetMapping
  @Operation(summary = "주문 목록 조회", description = "내 주문 목록을 페이지 단위로 조회합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "주문 목록 조회 성공")
  public ResponseEntity<ApiResponse<ApiResponse.PageResponse<OrderListItemResponse>>> findOrders(
      @AuthenticationPrincipal String email,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);

    Page<OrderListItemResponse> response = orderFacadeService.findOrdersByUserId(email, pageable);

    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /**
   * 주문 수동 취소.
   *
   * @param email 인증된 사용자 이메일
   * @param orderId 주문 ID
   * @return 취소된 주문 응답
   */
  @PatchMapping("/{orderId}/cancel")
  @Operation(summary = "주문 취소", description = "결제 전 주문을 직접 취소합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "주문 취소 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "취소 불가한 주문 상태",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
      @AuthenticationPrincipal String email, @PathVariable Long orderId) {
    return ResponseEntity.ok(ApiResponse.ok(orderFacadeService.cancelOrder(orderId, email)));
  }
}
