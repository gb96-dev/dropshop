package com.example.dropshop.domain.wishlist.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 찜 요청 DTO.
 */
@Getter
@AllArgsConstructor
@Builder
public class WishlistRequest {

  private Long dropId;
}
