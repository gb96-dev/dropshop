package com.example.dropshop.domain.drops.entity;

import com.example.dropshop.common.entity.BaseEntity;
import com.example.dropshop.domain.drops.exception.DropsErrorCode;
import com.example.dropshop.domain.drops.exception.DropsException;
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
public class Drops extends BaseEntity {

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

    @Builder(access = AccessLevel.PRIVATE)
    private Drops(
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

    public static Drops create(
            Product product,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long totalStock,
            Long purchaseLimit,
            boolean useQueue
    ) {
        return Drops.builder()
                .product(product)
                .startAt(startAt)
                .endAt(endAt)
                .totalStock(totalStock)
                .remainStock(totalStock)
                .purchaseLimit(purchaseLimit)
                .useQueue(useQueue)
                .build();
    }

    public void activate() {
        this.status = DropStatus.ACTIVE;
    }

    public void finish() {
        this.status = DropStatus.FINISHED;
    }

    private void validateDateRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new DropsException(DropsErrorCode.INVALID_DATE_RANGE);
        }
    }

    private void validateStock(Long totalStock, Long remainStock) {
        if (totalStock == null || totalStock <= 0) {
            throw new DropsException(DropsErrorCode.INVALID_TOTAL_STOCK);
        }
        if (remainStock == null || remainStock < 0 || remainStock > totalStock) {
            throw new DropsException(DropsErrorCode.INVALID_REMAIN_STOCK);
        }
    }

    private void validatePurchaseLimit(Long purchaseLimit) {
        if (purchaseLimit == null || purchaseLimit <= 0) {
            throw new DropsException(DropsErrorCode.INVALID_PURCHASE_LIMIT);
        }
    }
}



