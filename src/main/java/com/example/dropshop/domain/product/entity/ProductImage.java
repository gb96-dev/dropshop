package com.example.dropshop.domain.product.entity;

import com.example.dropshop.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean isThumbnail;

    @Builder
    private ProductImage(Product product, String imageUrl,
                         int sortOrder, boolean isThumbnail) {
        this.product     = product;
        this.imageUrl    = imageUrl;
        this.sortOrder   = sortOrder;
        this.isThumbnail = isThumbnail;
    }

    public void markAsThumbnail() {
        this.isThumbnail = true;
    }

    public void unmarkAsThumbnail() {
        this.isThumbnail = false;
    }

    public void updateSortOrder(int newSortOrder) {
        this.sortOrder = newSortOrder;
    }
}
