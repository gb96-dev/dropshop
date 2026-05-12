package com.example.dropshop.domain.refund.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.refund.dto.request.RefundCreateRequest;
import com.example.dropshop.domain.refund.dto.response.RefundResponse;
import com.example.dropshop.domain.refund.facade.RefundFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 환불 REST 컨트롤러. */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Tag(name = "Refund", description = "환불 API")
@SecurityRequirement(name = "bearerAuth")
public class RefundController {

  private final RefundFacadeService refundFacadeService;

  /**
   * 환불 요청을 생성한다.
   *
   * @param email 인증된 사용자 이메일
   * @param request 환불 생성 요청
   * @return 생성된 환불 응답
   */
  @PostMapping
  @Operation(summary = "환불 요청 생성", description = "결제 완료 건에 대한 환불 요청을 생성합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "환불 요청 생성 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "환불 불가 상태 또는 금액 불일치",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<RefundResponse>> createRefund(
      @AuthenticationPrincipal String email, @RequestBody @Valid RefundCreateRequest request) {
    return ResponseEntity.status(201)
        .body(ApiResponse.created(refundFacadeService.createRefund(email, request)));
  }

  /**
   * 환불 단건을 조회한다.
   *
   * @param email 인증된 사용자 이메일
   * @param refundId 환불 ID
   * @return 환불 응답
   */
  @GetMapping("/{refundId}")
  @Operation(summary = "환불 단건 조회", description = "환불 ID로 환불 상세를 조회합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "환불 조회 성공")
  public ResponseEntity<ApiResponse<RefundResponse>> getRefund(
      @AuthenticationPrincipal String email, @PathVariable Long refundId) {
    return ResponseEntity.ok(ApiResponse.ok(refundFacadeService.getRefund(refundId, email)));
  }

  /**
   * 결제에 연결된 환불 목록을 조회한다.
   *
   * @param email 인증된 사용자 이메일
   * @param paymentId 결제 ID
   * @return 환불 목록 응답
   */
  @GetMapping("/payments/{paymentId}")
  @Operation(summary = "결제별 환불 목록 조회", description = "특정 결제에 연결된 환불 요청 목록을 조회합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "환불 목록 조회 성공")
  public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundsByPayment(
      @AuthenticationPrincipal String email, @PathVariable Long paymentId) {
    return ResponseEntity.ok(
        ApiResponse.ok(refundFacadeService.getRefundsByPayment(paymentId, email)));
  }

  /**
   * 환불을 승인한다.
   *
   * @param email 인증된 사용자 이메일
   * @param refundId 환불 ID
   * @return 승인된 환불 응답
   */
  @PostMapping("/{refundId}/approve")
  @Operation(summary = "환불 승인", description = "대기 중인 환불 요청을 승인 상태로 변경합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "환불 승인 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "환불 상태 전이 불가",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<RefundResponse>> approveRefund(
      @AuthenticationPrincipal String email, @PathVariable Long refundId) {
    return ResponseEntity.ok(ApiResponse.ok(refundFacadeService.approveRefund(refundId, email)));
  }

  /**
   * 환불을 완료한다.
   *
   * @param email 인증된 사용자 이메일
   * @param refundId 환불 ID
   * @return 완료된 환불 응답
   */
  @PostMapping("/{refundId}/complete")
  @Operation(summary = "환불 완료", description = "PG 환불을 수행하고 내부 환불을 완료 처리합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "환불 완료 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "환불 상태 불일치 또는 PG 환불 실패",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<RefundResponse>> completeRefund(
      @AuthenticationPrincipal String email, @PathVariable Long refundId) {
    return ResponseEntity.ok(ApiResponse.ok(refundFacadeService.completeRefund(refundId, email)));
  }

  /**
   * 환불을 거절한다.
   *
   * @param email 인증된 사용자 이메일
   * @param refundId 환불 ID
   * @return 거절된 환불 응답
   */
  @PostMapping("/{refundId}/reject")
  @Operation(summary = "환불 거절", description = "대기 중인 환불 요청을 거절합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "환불 거절 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "환불 상태 전이 불가",
        content = @Content(schema = @Schema(hidden = true)))
  })
  public ResponseEntity<ApiResponse<RefundResponse>> rejectRefund(
      @AuthenticationPrincipal String email, @PathVariable Long refundId) {
    return ResponseEntity.ok(ApiResponse.ok(refundFacadeService.rejectRefund(refundId, email)));
  }
}
