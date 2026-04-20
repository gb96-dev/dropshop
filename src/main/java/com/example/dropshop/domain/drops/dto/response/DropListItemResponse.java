package com.example.dropshop.domain.drops.dto.response;

import com.example.dropshop.domain.drops.entity.Drops;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 드랍 목록 조회 응답 DTO.
 */
@Getter
@Builder
public class DropListItemResponse {

  private Long dropId;
  private Long productId;
  private String productName;
  private String thumbnailUrl;
  private String status;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private Long soldCount;
  private Long remainStock;
  private Long purchaseLimit;
  private boolean useQueue;

  /**
   * 드랍 엔티티를 목록 응답 DTO로 변환한다.
   */
  public static DropListItemResponse from(Drops drops) {
    return DropListItemResponse.builder()
        .dropId(drops.getId())
        .productId(drops.getProduct().getId())
        .productName(drops.getProduct().getName())
        .thumbnailUrl(drops.getProduct().getThumbnailUrl())
        .status(drops.getStatus().name())
        .startAt(drops.getStartAt())
        .endAt(drops.getEndAt())
        .soldCount(drops.getTotalStock() - drops.getRemainStock())
        .remainStock(drops.getRemainStock())
        .purchaseLimit(drops.getPurchaseLimit())
        .useQueue(drops.isUseQueue())
        .build();
  }
}


