package com.example.dropshop.domain.terms.dto.response;

import com.example.dropshop.domain.terms.entity.Terms;

public record TermsResponse(
    Long id, String title, String content, Boolean isRequired, String version) {
  // Terms 엔티티를 받아서 Response record로 변환해주는 메서드 추가
  public static TermsResponse from(Terms terms) {
    return new TermsResponse(
        terms.getId(),
        terms.getTitle(),
        terms.getContent(),
        terms.getIsRequired(),
        terms.getVersion());
  }
}
