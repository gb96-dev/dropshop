package com.example.dropshop.domain.seller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SellerUpdateRequest {
    @NotBlank(message = "브랜드 이름은 필수입니다.")
    private String brandName;

    private String brandLogo;

    @NotBlank(message = "정산 계좌 정보는 필수입니다.")
    private String accountInfo;
}