package com.example.dropshop.domain.product.entity;

import com.example.dropshop.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "products",
        indexes = {
                // 상품 목록 조회: status 필터 + created_at 정렬이 항상 함께 사용
                @Index(name = "idx_products_status_created",
                        columnList = "status, created_at"),

                // 상품 목록 조회: status 필터 + 가격 정렬
                @Index(name = "idx_products_status_price",
                        columnList = "status, sale_price"),

                // 판매자 본인 상품 조회: seller_id + status 조합
                @Index(name = "idx_products_seller_status",
                        columnList = "seller_id, status"),
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    private static final int MAX_IMAGE_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    /** 기준 판매가 (할인 전 원가) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 할인율 (0 이상 100 미만). 할인 없을 경우 0. */
    @Column(nullable = false)
    private int discountRate;

    /** 할인 금액 (price * discountRate / 100, 내림 처리). 자동 계산값 — 직접 입력 금지. */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    /** 실제 판매가 (price - discountAmount). 자동 계산값 — 직접 입력 금지. */
    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

    /** 상품 보유 재고 (드랍 total_stock 은 이 값을 초과할 수 없음) */
    @Column(nullable = false)
    private int stock;

    /**
     * 대표 썸네일 URL.
     * 이미지 추가 시 {@link #addImage}를 통해 isThumbnail=true 이미지와 자동 동기화된다.
     */
    @Column(nullable = false, length = 500)
    private String thumbnailUrl;

    /** 상품 전체 설명 (이미지 포함 HTML/Markdown 허용) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** 상품 사양 / 상세 정보 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String specification;

    /**
     * 배송 안내.
     * 운영 정책상 공통 내용이지만, 향후 판매자별 커스터마이징을 고려해 상품 단위로 저장한다.
     * 공통 기본값은 서비스 레이어에서 주입한다.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String deliveryInfo;

    /**
     * 환불 정책.
     * 배송 안내와 동일한 이유로 상품 단위로 저장한다.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String refundPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    /**
     * 상품 이미지 목록 (최대 {@value MAX_IMAGE_COUNT}개).
     * 이미지 추가는 반드시 {@link #addImage}를 통해 수행해야 한다.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    // -------------------------------------------------------------------------
    // 생성자 (Builder)
    // -------------------------------------------------------------------------

    /**
     * discountAmount / salePrice 는 price 와 discountRate 로부터 자동 계산된다.
     * 도메인 규칙 5번: 최종 금액은 원가보다 높을 수 없으며, 0 미만이 될 수 없다.
     */
    @Builder
    private Product(Long sellerId, String name, String category,
                    BigDecimal price, int discountRate,
                    int stock, String thumbnailUrl, String description,
                    String specification, String deliveryInfo, String refundPolicy) {
        validatePrice(price);
        validateDiscountRate(discountRate);

        this.sellerId      = sellerId;
        this.name          = name;
        this.category      = category;
        this.price         = price;
        this.discountRate  = discountRate;
        this.stock         = stock;
        this.thumbnailUrl  = thumbnailUrl;
        this.description   = description;
        this.specification = specification;
        this.deliveryInfo  = deliveryInfo;
        this.refundPolicy  = refundPolicy;
        this.status        = ProductStatus.HIDDEN; // 드랍 미지정 시 기본값 (규칙 8번)

        // discountAmount / salePrice 자동 계산
        recalculatePrices(price, discountRate);
    }

    // -------------------------------------------------------------------------
    // 가격 수정
    // -------------------------------------------------------------------------

    /**
     * 상품 가격 및 할인율 수정.
     * 드랍 활성 상태(READY / ON_SALE / OUT_OF_STOCK)에서는 서비스 레이어에서 {@link #isCoreLocked()} 로 사전 차단.
     */
    public void updatePrice(BigDecimal newPrice, int newDiscountRate) {
        validatePrice(newPrice);
        validateDiscountRate(newDiscountRate);
        this.price         = newPrice;
        this.discountRate  = newDiscountRate;
        recalculatePrices(newPrice, newDiscountRate);
    }

    // -------------------------------------------------------------------------
    // 상태 변경
    // -------------------------------------------------------------------------

    /**
     * 판매자가 수동으로 상품을 비공개(HIDDEN) 처리한다.
     * READY(드랍 시작 전)는 숨김 전환 가능하며,
     * 실제 판매가 진행/종료 처리된 상태(ON_SALE / OUT_OF_STOCK)에서는 허용하지 않는다.
     */
    public void hide() {
        if (this.status == ProductStatus.ON_SALE || this.status == ProductStatus.OUT_OF_STOCK) {
            throw new IllegalStateException("판매 중이거나 품절 상태의 상품은 비공개로 변경할 수 없습니다.");
        }
        this.status = ProductStatus.HIDDEN;
    }

    /**
     * 스케줄러 / 이벤트에 의한 자동 상태 전이 전용.
     * 드랍 시작 → READY→ON_SALE, 재고 소진 → OUT_OF_STOCK, 드랍 종료 → HIDDEN 복원 등에 사용.
     */
    public void updateStatusByDrop(ProductStatus newStatus) {
        this.status = newStatus;
    }

    // -------------------------------------------------------------------------
    // 이미지 관리
    // -------------------------------------------------------------------------

    /**
     * 이미지 추가 (최대 {@value MAX_IMAGE_COUNT}개 제한 — 규칙 14번).
     * {@code image.isThumbnail() == true} 이면 {@code thumbnailUrl}을 자동 갱신한다.
     */
    public void addImage(ProductImage image) {
        if (this.images.size() >= MAX_IMAGE_COUNT) {
            throw new IllegalStateException(
                    "상품 이미지는 최대 " + MAX_IMAGE_COUNT + "개까지 등록 가능합니다.");
        }
        this.images.add(image);
        if (image.isThumbnail()) {
            syncThumbnailUrl(image.getImageUrl());
        }
    }

    /**
     * 특정 이미지를 대표 썸네일로 지정한다.
     * 기존 썸네일은 해제되며, {@code thumbnailUrl} 필드도 함께 갱신된다.
     */
    public void setThumbnail(ProductImage newThumbnail) {
        this.images.forEach(img -> {
            if (img.isThumbnail()) img.unmarkAsThumbnail();
        });
        newThumbnail.markAsThumbnail();
        syncThumbnailUrl(newThumbnail.getImageUrl());
    }

    // -------------------------------------------------------------------------
    // 소유권 / 잠금 판별
    // -------------------------------------------------------------------------

    /** 판매자 소유 여부 검증 (규칙: product.sellerId === auth.userId) */
    public boolean isOwnedBy(Long sellerId) {
        return this.sellerId.equals(sellerId);
    }

    /**
     * 핵심 필드(상품명·가격·할인율 등) 수정 잠금 여부.
     * READY / ON_SALE : 드랍 예약 또는 판매 진행 중 → 잠금 (규칙 6번)
     * OUT_OF_STOCK    : 드랍 재고 소진 상태 — 해당 드랍은 종료되지 않았으므로 잠금 유지
     */
    public boolean isCoreLocked() {
        return this.status == ProductStatus.READY
                || this.status == ProductStatus.ON_SALE
                || this.status == ProductStatus.OUT_OF_STOCK;
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private void recalculatePrices(BigDecimal basePrice, int rate) {
        BigDecimal rateDecimal = BigDecimal.valueOf(rate).divide(BigDecimal.valueOf(100), 10, RoundingMode.DOWN);
        BigDecimal discount    = basePrice.multiply(rateDecimal).setScale(0, RoundingMode.DOWN);
        BigDecimal sale        = basePrice.subtract(discount);

        this.discountAmount = discount;
        this.salePrice      = sale.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : sale;
    }

    private void syncThumbnailUrl(String url) {
        this.thumbnailUrl = url;
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("상품 가격은 0원보다 커야 합니다.");
        }
    }

    private static void validateDiscountRate(int rate) {
        if (rate < 0 || rate >= 100) {
            throw new IllegalArgumentException("할인율은 0 이상 100 미만이어야 합니다.");
        }
    }
}
