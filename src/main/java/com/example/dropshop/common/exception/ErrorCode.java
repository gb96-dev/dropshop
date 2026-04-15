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

  // USER
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영문, 숫자, 특수문자를 포함한 8~16자여야 합니다."),
  PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다."),
  ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "이미 탈퇴 처리된 사용자입니다."),

  // SELLER
  SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자 정보를 찾을 수 없습니다."),
  DUPLICATE_BUSINESS_NO(HttpStatus.CONFLICT, "이미 등록된 사업자 번호입니다."),
  DUPLICATE_BRAND_NAME(HttpStatus.CONFLICT, "이미 존재하는 브랜드 이름입니다."),



  /**
   * Order (세미 콜론 부분 변경 금지)
   */
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  ORDER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 주문만 취소할 수 있습니다.");

  private final HttpStatus status;
  private final String message;
}
