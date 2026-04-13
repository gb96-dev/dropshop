package com.example.dropshop.domain.drops.entity;

import com.example.dropshop.common.entity.BaseEntity;
import com.example.dropshop.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "drops",
        indexes = {
                @Index(name = "idx_drops_product_id", columnList = "product_id"),
                @Index(name = "idx_drops_status", columnList = "status"),
                @Index(name = "idx_drops_start_at", columnList = "start_at"),
                @Index(name = "idx_drops_end_at", columnList = "end_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Drop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DropStatus status;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "total_stock", nullable = false)
    private Long totalStock;

    @Column(name = "remain_stock", nullable = false)
    private Long remainStock;

    @Column(name = "purchase_limit", nullable = false)
    private Long purchaseLimit;

    @Column(name = "use_queue", nullable = false)
    private boolean useQueue;

    @Builder
    private Drop(
            Product product,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long totalStock,
            Long remainStock,
            Long purchaseLimit,
            boolean useQueue
    ) {
        this.product = product;
        this.startAt = startAt;
        this.endAt = endAt;
        this.totalStock = totalStock;
        this.remainStock = remainStock;
        this.purchaseLimit = purchaseLimit;
        this.useQueue = useQueue;
        this.status = DropStatus.SCHEDULED;

        validateDateRange(startAt, endAt);
        validateStock(totalStock, remainStock);
        validatePurchaseLimit(purchaseLimit);
    }

    public void activate() {
        this.status = DropStatus.ACTIVE;
    }

    public void finish() {
        this.status = DropStatus.FINISHED;
    }

    private void validateDateRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("드랍 종료 시간은 시작 시간보다 뒤여야 합니다.");
        }
    }

    private void validateStock(Long totalStock, Long remainStock) {
        if (totalStock == null || totalStock <= 0) {
            throw new IllegalArgumentException("드랍 총 판매 수량은 0보다 커야 합니다.");
        }
        if (remainStock == null || remainStock < 0 || remainStock > totalStock) {
            throw new IllegalArgumentException("잔여 수량은 0 이상이며 총 판매 수량 이하여야 합니다.");
        }
    }

    private void validatePurchaseLimit(Long purchaseLimit) {
        if (purchaseLimit == null || purchaseLimit <= 0) {
            throw new IllegalArgumentException("1인당 구매 제한 수량은 1 이상이어야 합니다.");
        }
    }
}

