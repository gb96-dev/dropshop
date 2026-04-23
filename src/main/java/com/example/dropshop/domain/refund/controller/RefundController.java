package com.example.dropshop.domain.refund.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.refund.dto.request.RefundCreateRequest;
import com.example.dropshop.domain.refund.dto.response.RefundResponse;
import com.example.dropshop.domain.refund.facade.RefundFacadeService;
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

/**
 * 환불 요청, 조회, 상태 변경을 처리하는 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

  private final RefundFacadeService refundFacadeService;

  /**
   * 환불 요청을 생성한다.
   *
   * @param request 환불 생성 요청
   * @return 생성된 환불 응답
   */
  @PostMapping
  public ResponseEntity<ApiResponse<RefundResponse>> createRefund(
      @AuthenticationPrincipal String email,
      @RequestBody @Valid RefundCreateRequest request
  ) {
    return ResponseEntity.status(201)
        .body(ApiResponse.created(
            refundFacadeService.createRefund(email, request)
        ));
  }

  /**
   * 환불 단건을 조회한다.
   *
   * @param refundId 환불 ID
   * @return 환불 응답
   */
  @GetMapping("/{refundId}")
  public ResponseEntity<ApiResponse<RefundResponse>> getRefund(
      @AuthenticationPrincipal String email,
      @PathVariable Long refundId
  ) {
    return ResponseEntity.ok(ApiResponse.ok(
        refundFacadeService.getRefund(refundId, email)
    ));
  }

  /**
   * 결제에 연결된 환불 목록을 조회한다.
   *
   * @param paymentId 결제 ID
   * @return 환불 목록 응답
   */
  @GetMapping("/payments/{paymentId}")
  public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundsByPayment(
      @AuthenticationPrincipal String email,
      @PathVariable Long paymentId
  ) {
    return ResponseEntity.ok(ApiResponse.ok(
        refundFacadeService.getRefundsByPayment(paymentId, email)
    ));
  }

  /**
   * 환불을 승인한다.
   *
   * @param refundId 환불 ID
   * @return 승인된 환불 응답
   */
  @PostMapping("/{refundId}/approve")
  public ResponseEntity<ApiResponse<RefundResponse>> approveRefund(
      @AuthenticationPrincipal String email,
      @PathVariable Long refundId
  ) {
    return ResponseEntity.ok(ApiResponse.ok(refundFacadeService.approveRefund(refundId, email)));
  }

  /**
   * 환불을 완료한다.
   *
   * @param refundId 환불 ID
   * @return 완료된 환불 응답
   */
  @PostMapping("/{refundId}/complete")
  public ResponseEntity<ApiResponse<RefundResponse>> completeRefund(
      @AuthenticationPrincipal String email,
      @PathVariable Long refundId
  ) {
    return ResponseEntity.ok(ApiResponse.ok(refundFacadeService.completeRefund(refundId, email)));
  }

  /**
   * 환불을 거절한다.
   *
   * @param refundId 환불 ID
   * @return 거절된 환불 응답
   */
  @PostMapping("/{refundId}/reject")
  public ResponseEntity<ApiResponse<RefundResponse>> rejectRefund(
      @AuthenticationPrincipal String email,
      @PathVariable Long refundId
  ) {
    return ResponseEntity.ok(ApiResponse.ok(refundFacadeService.rejectRefund(refundId, email)));
  }
}
