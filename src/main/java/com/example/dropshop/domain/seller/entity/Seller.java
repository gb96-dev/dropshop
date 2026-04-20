package com.example.dropshop.domain.seller.entity;

import com.example.dropshop.common.entity.BaseEntity;
import com.example.dropshop.domain.seller.enums.SellerStatus;
import com.example.dropshop.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10, unique = true)
    private String businessNo; // 사업자 번호 10자리

    @Column(nullable = false)
    private String brandName;

    private String brandLogo;

    @Column(nullable = false)
    private String accountInfo; // 정산 계좌 정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerStatus status;

    @Builder
    public Seller(User user, String businessNo, String brandName, String brandLogo, String accountInfo) {
        this.user = user;
        this.businessNo = businessNo;
        this.brandName = brandName;
        this.brandLogo = brandLogo;
        this.accountInfo = accountInfo;
        this.status = SellerStatus.PENDING;
    }

    public void updateInfo(String brandName, String brandLogo, String accountInfo) {
        this.brandName = brandName;
        this.brandLogo = brandLogo;
        this.accountInfo = accountInfo;
    }

    public void approve() {
        this.status = SellerStatus.APPROVED;
    }
}