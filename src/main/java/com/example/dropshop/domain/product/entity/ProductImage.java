package com.example.dropshop.domain.product.entity;

import com.example.dropshop.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 상품 이미지 엔티티. */
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
  private ProductImage(Product product, String imageUrl, int sortOrder, boolean isThumbnail) {
    this.product = product;
    this.imageUrl = imageUrl;
    this.sortOrder = sortOrder;
    this.isThumbnail = isThumbnail;
  }

  /** 현재 이미지를 대표 이미지로 지정한다. */
  public void markAsThumbnail() {
    this.isThumbnail = true;
  }

  /** 현재 이미지의 대표 이미지 지정을 해제한다. */
  public void unmarkAsThumbnail() {
    this.isThumbnail = false;
  }

  /**
   * 노출 순서를 변경한다.
   *
   * @param newSortOrder 변경할 정렬 순서
   */
  public void updateSortOrder(int newSortOrder) {
    this.sortOrder = newSortOrder;
  }
}
