package com.example.dropshop.domain.seller.entity;

import com.example.dropshop.common.entity.BaseEntity;
import com.example.dropshop.domain.seller.enums.SellerStatus;
import com.example.dropshop.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Seller extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10, unique = true)
    private String businessNo;

    @Column(nullable = false)
    private String brandName;

    private String brandLogo;

    @Column(nullable = false)
    private String accountInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default // 빌더 사용 시 값이 없으면 PENDING으로 설정
    private SellerStatus status = SellerStatus.PENDING;

    public void updateInfo(String brandName, String brandLogo, String accountInfo) {
        this.brandName = brandName;
        this.brandLogo = brandLogo;
        this.accountInfo = accountInfo;
    }

    public void approve() {
        this.status = SellerStatus.APPROVED;
    }

    public void suspend() {
        this.status = SellerStatus.SUSPENDED;
    }
}