package com.example.dropshop.domain.wishlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 찜 요청 DTO. */
@Getter
@AllArgsConstructor
public class WishlistRequest {

  @Schema(description = "찜할 드랍 ID", example = "1")
  private Long dropId;
}
