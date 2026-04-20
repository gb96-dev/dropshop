package com.example.dropshop.domain.drops.dto.response;

import com.example.dropshop.domain.drops.entity.Drops;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 드랍 응답 DTO.
 */
@Getter
@Builder
public class DropResponse {

  private Long dropId;
  private Long productId;
  private String status;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private Long totalStock;
  private Long remainStock;
  private Long purchaseLimit;
  private boolean useQueue;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

  /**
   * 드랍 엔티티를 응답 DTO로 변환한다.
   */
  public static DropResponse from(Drops drops) {
    return DropResponse.builder()
        .dropId(drops.getId())
        .productId(drops.getProduct().getId())
        .status(drops.getStatus().name())
        .startAt(drops.getStartAt())
        .endAt(drops.getEndAt())
        .totalStock(drops.getTotalStock())
        .remainStock(drops.getRemainStock())
        .purchaseLimit(drops.getPurchaseLimit())
        .useQueue(drops.isUseQueue())
        .createdAt(drops.getCreatedAt())
        .modifiedAt(drops.getModifiedAt())
        .build();
  }
}

