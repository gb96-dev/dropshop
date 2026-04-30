package com.example.dropshop.domain.seller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerApplyRequest {
    @NotBlank(message = "회사명을 입력해주세요.")
    private String companyName;

    @NotBlank(message = "대표자명을 입력해주세요.")
    private String representativeName;

    @NotBlank(message = "사업자 번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{10}$", message = "사업자 번호는 숫자 10자리여야 합니다.")
    private String businessNo;

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phoneNumber;

    @NotBlank(message = "브랜드명을 입력해주세요.")
    private String brandName;

    private String brandLogo;

    @NotBlank(message = "정산 계좌 정보를 입력해주세요.")
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