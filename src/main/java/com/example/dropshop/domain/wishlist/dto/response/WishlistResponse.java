package com.example.dropshop.domain.wishlist.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class WishlistResponse {

  private Long dropId;
  private LocalDateTime createdAt;
}
