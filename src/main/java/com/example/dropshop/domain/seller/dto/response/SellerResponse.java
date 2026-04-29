package com.example.dropshop.domain.seller.dto.response;

import com.example.dropshop.domain.seller.entity.Seller;
import lombok.Getter;

@Getter
public class SellerResponse {
    private final Long id;
    private final String companyName;
    private final String representativeName;
    private final String phoneNumber;
    private final String businessNo;
    private final String brandName;
    private final String brandLogo;
    private final com.example.dropshop.domain.seller.enums.SellerStatus status;

    public SellerResponse(Seller seller) {
        this.id = seller.getId();
        this.companyName = seller.getCompanyName();
        this.representativeName = seller.getRepresentativeName();
        this.phoneNumber = seller.getPhoneNumber();
        this.businessNo = seller.getBusinessNo();
        this.brandName = seller.getBrandName();
        this.brandLogo = seller.getBrandLogo();
        this.status = seller.getStatus();
    }
}