package com.example.dropshop.domain.seller.dto.response;

import com.example.dropshop.domain.seller.entity.Seller;
import lombok.Getter;

@Getter
public class SellerResponse {
    private final Long id;
    private final String businessNo;
    private final String brandName;
    private final String brandLogo;
    private final com.example.dropshop.domain.seller.enums.SellerStatus status;

    public SellerResponse(Seller seller) {
        this.id = seller.getId();
        this.businessNo = seller.getBusinessNo();
        this.brandName = seller.getBrandName();
        this.brandLogo = seller.getBrandLogo();
        this.status = seller.getStatus();
    }
}