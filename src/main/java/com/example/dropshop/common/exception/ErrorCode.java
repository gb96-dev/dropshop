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
  ORDER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 주문만 취소할 수 있습니다."),
  ORDER_DUPLICATE(HttpStatus.CONFLICT, "이미 존재하는 주문입니다."),
  ORDER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태 전이입니다."),
  ORDER_HOLD_EXPIRED(HttpStatus.BAD_REQUEST, "주문 홀드 시간이 만료되었습니다."),


  // Wishlist
  EXISTS_BY_USER_AND_DROP(HttpStatus.CONFLICT, "이미 찜한 상품입니다."),
  DROP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 드랍은 존재하지 않습니다.");


  private final HttpStatus status;
  private final String message;
}
