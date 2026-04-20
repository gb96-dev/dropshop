package com.example.dropshop.domain.seller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SellerApplyRequest {
    @NotBlank(message = "사업자 번호는 필수입니다.")
    @Pattern(regexp = "\\d{10}", message = "사업자 번호는 10자리 숫자여야 합니다.")
    private String businessNo;

    @NotBlank(message = "브랜드 이름은 필수입니다.")
    private String brandName;

    private String brandLogo;

    @NotBlank(message = "정산 계좌 정보는 필수입니다.")
    private String accountInfo;
}