package com.example.dropshop.domain.seller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerApplyRequest {
  @NotBlank private String companyName;
  @NotBlank private String representativeName;

  @NotBlank
  @Pattern(regexp = "\\d{10}", message = "사업자 번호는 숫자 10자리여야 합니다.")
  private String businessNo;

  @NotBlank private String phoneNumber;
  @NotBlank private String brandName;
  private String brandLogo;
  @NotBlank private String accountInfo;

  public SellerApplyRequest(
      String companyName,
      String representativeName,
      String businessNo,
      String phoneNumber,
      String brandName,
      String brandLogo,
      String accountInfo) {
    this.companyName = companyName;
    this.representativeName = representativeName;
    this.businessNo = businessNo;
    this.phoneNumber = phoneNumber;
    this.brandName = brandName;
    this.brandLogo = brandLogo;
    this.accountInfo = accountInfo;
  }
}
