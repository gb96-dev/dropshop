package com.example.dropshop.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  /**
   * Product.
   */
  INVALID_PRICE(HttpStatus.BAD_REQUEST, "상품 가격이 0 이하입니다."),
  INVALID_DISCOUNT_RATE(HttpStatus.BAD_REQUEST, "할인율은 0~100 사이여야 합니다."),
  IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "이미지는 최소 1개 이상 등록해야 합니다."),
  IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 5개까지 등록 가능합니다."),
  THUMBNAIL_REQUIRED(HttpStatus.BAD_REQUEST, "대표 이미지(isThumbnail=true)가 정확히 1개여야 합니다."),
  PRODUCT_HIDE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "판매 중이거나 품절 상태의 상품은 비공개로 변경할 수 없습니다."),
  SELLER_NOT_VERIFIED(HttpStatus.FORBIDDEN, "정산 계좌 인증이 완료되지 않았습니다."),
  SELLER_NOT_APPROVED(HttpStatus.FORBIDDEN, "판매자 승인 대기 상태입니다."),
  SELLER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "판매자 권한이 필요합니다."),
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
  PRODUCT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인 상품만 수정 또는 삭제할 수 있습니다."),
  PRODUCT_CORE_UPDATE_LOCKED(HttpStatus.BAD_REQUEST,
      "READY 또는 ON_SALE 상태에서는 상품명/가격/할인율을 수정할 수 없습니다."),
  INVALID_PRODUCT_STATUS_CHANGE(HttpStatus.BAD_REQUEST,
      "판매자 수동 상태 변경은 HIDDEN만 가능합니다."),
  PRODUCT_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST,
      "드랍 또는 주문 이력이 존재하는 상품은 삭제할 수 없습니다."),
  INVALID_STOCK(HttpStatus.BAD_REQUEST, "상품 재고는 0보다 커야 합니다."),
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청값 검증에 실패했습니다."),

  /**
   * Drop.
   */
  INVALID_DROP_DATE_RANGE(HttpStatus.BAD_REQUEST, "드랍 종료 시간은 시작 시간보다 뒤여야 합니다."),
  INVALID_DROP_TOTAL_STOCK(HttpStatus.BAD_REQUEST, "드랍 총 판매 수량은 0보다 커야 합니다."),
  INVALID_DROP_REMAIN_STOCK(HttpStatus.BAD_REQUEST, "잔여 수량은 0 이상이며 총 판매 수량 이하여야 합니다."),
  INVALID_DROP_PURCHASE_LIMIT(HttpStatus.BAD_REQUEST, "1인당 구매 제한 수량은 1 이상이어야 합니다."),

  /**
   * Order (세미 콜론 부분 변경 금지).
   */
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  ORDER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 주문만 취소할 수 있습니다.");

  private final HttpStatus status;
  private final String message;
}
