package com.example.dropshop.domain.wishlist.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 찜 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDto {

  private Long userId;

  private Long dropId;

  private LocalDateTime createdAt;
}
