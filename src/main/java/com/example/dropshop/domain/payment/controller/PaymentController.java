package com.example.dropshop.domain.payment.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.dropshop.domain.payment.dto.request.PaymentPrepareRequest;
import com.example.dropshop.domain.payment.dto.request.PaymentWebhookRequest;
import com.example.dropshop.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.dropshop.domain.payment.dto.response.PaymentPortOneRequestResponse;
import com.example.dropshop.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.dropshop.domain.payment.facade.PaymentFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 결제 REST 컨트롤러. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 API")
public class PaymentController {

  private final PaymentFacadeService paymentFacadeService;

  /**
   * 주문 결제를 준비하고 결제 엔티티를 생성한다.
   *
   * @param email 인증된 사용자 이메일
   * @param request 결제 준비 요청
   * @return 생성된 결제 정보 응답
   */
  @PostMapping("/prepare")
  @Operation(summary = "결제 준비", description = "주문에 대한 결제 엔티티를 생성합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "결제 준비 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "결제 금액 또는 주문 상태 불일치",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<PaymentPrepareResponse>> preparePayment(
      @AuthenticationPrincipal String email, @RequestBody @Valid PaymentPrepareRequest request) {
    return ResponseEntity.status(201)
        .body(ApiResponse.created(paymentFacadeService.preparePayment(email, request)));
  }

  /**
   * 프론트엔드의 PortOne 결제 요청에 필요한 정보를 조회한다.
   *
   * @param email 인증된 사용자 이메일
   * @param paymentId 결제 ID
   * @return PortOne 요청 정보 응답
   */
  @GetMapping("/{paymentId}/portone-request")
  @Operation(summary = "PortOne 요청 정보 조회", description = "프론트엔드 결제 요청에 필요한 PG 파라미터를 조회합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "결제 요청 정보 조회 성공")
  public ResponseEntity<ApiResponse<PaymentPortOneRequestResponse>> getPortOneRequest(
      @AuthenticationPrincipal String email, @PathVariable Long paymentId) {
    return ResponseEntity.ok(
        ApiResponse.ok(paymentFacadeService.getPortOneRequest(paymentId, email)));
  }

  /**
   * PortOne 결제 결과를 검증한 뒤 결제를 확정한다.
   *
   * @param email 인증된 사용자 이메일
   * @param paymentId 결제 ID
   * @param request 결제 확정 요청
   * @return 결제 확정 결과 응답
   */
  @PostMapping("/{paymentId}/confirm")
  @Operation(summary = "결제 확정", description = "PortOne 결제 결과를 검증하고 내부 결제를 확정합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "결제 확정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "PG 결제 정보 불일치 또는 결제 불가 상태",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
      @AuthenticationPrincipal String email,
      @PathVariable Long paymentId,
      @RequestBody @Valid PaymentConfirmRequest request) {
    return ResponseEntity.ok(
        ApiResponse.ok(paymentFacadeService.confirmPayment(paymentId, email, request)));
  }

  /**
   * PortOne 웹훅을 수신해 결제 상태를 동기화한다.
   *
   * @param request 웹훅 요청 본문
   * @return 처리 성공 응답
   */
  @PostMapping("/webhook")
  @Operation(summary = "PortOne 웹훅 수신", description = "PG 웹훅을 검증하고 내부 결제 상태를 동기화합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "웹훅 처리 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "웹훅 서명 검증 실패",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<Void>> handleWebhook(
      @RequestBody PaymentWebhookRequest request) {
    paymentFacadeService.handleWebhook(request);
    return ResponseEntity.ok(ApiResponse.ok());
  }
}
