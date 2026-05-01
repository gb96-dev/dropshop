package com.example.dropshop.common.config;

/**
 * Redis 캐시 이름 상수.
 */
public final class CacheNames {

  private CacheNames() {
  }

  /** 공개 상품 목록 캐시. TTL: 30초 */
  public static final String PRODUCT_LIST = "product:list";

  /** 공개 상품 상세 캐시. TTL: 60초 */
  public static final String PRODUCT_DETAIL = "product:detail";

  /** 상품별 최신 드랍 캐시. TTL: 30초 */
  public static final String DROP_LATEST = "drop:latest";
}

