package com.example.dropshop.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


  //ORDER
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  ORDER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "본인 주문만 취소할 수 있습니다."),//충돌 방지를 위해 해당 줄 변경하지 말아주세요

  // Wishlist
  EXISTS_BY_USER_AND_DROP(HttpStatus.CONFLICT, "이미 찜한 상품입니다."),
  DROP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 드랍은 존재하지 않습니다.");

  private final HttpStatus status;
  private final String message;
}
