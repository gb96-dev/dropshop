package com.example.dropshop.domain.seller.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerApplyRequest {
    private String companyName;
    private String representativeName;
    private String businessNo;
    private String phoneNumber;
    private String brandName;
    private String brandLogo;
    private String accountInfo; // ✅ 이번에 터진 필드 추가

    // 생성자 (모든 필드 포함)
    public SellerApplyRequest(String companyName, String representativeName,
                              String businessNo, String phoneNumber,
                              String brandName, String brandLogo, String accountInfo) {
        this.companyName = companyName;
        this.representativeName = representativeName;
        this.businessNo = businessNo;
        this.phoneNumber = phoneNumber;
        this.brandName = brandName;
        this.brandLogo = brandLogo;
        this.accountInfo = accountInfo;
    }
}