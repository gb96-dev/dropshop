package com.example.dropshop.domain.wishlist.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class WishlistRequest {
  private Long dropId;
}
