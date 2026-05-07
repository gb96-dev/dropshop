package com.example.dropshop.domain.refund.facade;

import com.example.dropshop.domain.refund.dto.request.RefundCreateRequest;
import com.example.dropshop.domain.refund.dto.response.RefundResponse;
import com.example.dropshop.domain.refund.entity.Refund;
import com.example.dropshop.domain.refund.service.RefundService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 환불 유스케이스 파사드 서비스.
 */
@Service
@RequiredArgsConstructor
public class RefundFacadeService {

  private final RefundService refundService;

  /**
   * 환불 요청을 생성하고 응답 DTO로 변환한다.
   *
   * @param email 인증된 사용자 이메일
   * @param request 환불 생성 요청
   * @return 환불 응답
   */
  public RefundResponse createRefund(String email, RefundCreateRequest request) {
    Refund refund = refundService.createRefund(
        email,
        request.getPaymentId(),
        request.getRefundAmount(),
        request.getRefundReason()
    );
    return RefundResponse.from(refund);
  }

  /**
   * 환불 단건을 조회하고 응답 DTO로 변환한다.
   *
   * @param refundId 환불 ID
   * @param email 인증된 사용자 이메일
   * @return 환불 응답
   */
  public RefundResponse getRefund(Long refundId, String email) {
    return RefundResponse.from(refundService.getRefund(refundId, email));
  }

  /**
   * 결제에 연결된 환불 목록을 조회하고 응답 DTO로 변환한다.
   *
   * @param paymentId 결제 ID
   * @param email 인증된 사용자 이메일
   * @return 환불 목록 응답
   */
  public List<RefundResponse> getRefundsByPayment(Long paymentId, String email) {
    return refundService.getRefundsByPayment(paymentId, email).stream()
        .map(RefundResponse::from)
        .toList();
  }

  /**
   * 환불을 승인하고 응답 DTO로 변환한다.
   *
   * @param refundId 환불 ID
   * @param email 인증된 사용자 이메일
   * @return 승인된 환불 응답
   */
  public RefundResponse approveRefund(Long refundId, String email) {
    return RefundResponse.from(refundService.approveRefund(refundId, email));
  }

  /**
   * 환불을 완료하고 응답 DTO로 변환한다.
   *
   * @param refundId 환불 ID
   * @param email 인증된 사용자 이메일
   * @return 완료된 환불 응답
   */
  public RefundResponse completeRefund(Long refundId, String email) {
    return RefundResponse.from(refundService.completeRefund(refundId, email));
  }

  /**
   * 환불을 거절하고 응답 DTO로 변환한다.
   *
   * @param refundId 환불 ID
   * @param email 인증된 사용자 이메일
   * @return 거절된 환불 응답
   */
  public RefundResponse rejectRefund(Long refundId, String email) {
    return RefundResponse.from(refundService.rejectRefund(refundId, email));
  }
}
