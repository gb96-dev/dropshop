package com.example.dropshop.domain.seller.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 판매자 신청 시 발행되는 Kafka 이벤트. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SellerAppliedEvent {

  private String email;
  private String companyName;
  private String businessNo;
  private LocalDateTime appliedAt;

  public static SellerAppliedEvent of(String email, String companyName, String businessNo) {
    return new SellerAppliedEvent(email, companyName, businessNo, LocalDateTime.now());
  }
}
