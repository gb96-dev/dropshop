package com.example.dropshop.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/** 주문 생성 요청. */
@Getter
public class OrderCreateRequest {

  @NotNull private Long dropId;

  @NotNull private Long productId;

  @NotNull private String queueToken;
}
