package com.example.dropshop.domain.terms.dto.response;

public record TermsResponse(
        Long id,
        String title,
        String content,
        Boolean isRequired,
        String version
) {
    public static TermsResponse from(com.example.dropshop.domain.terms.entity.Terms terms) {
        return new TermsResponse(
                terms.getId(),
                terms.getTitle(),
                terms.getContent(),
                terms.getIsRequired(),
                terms.getVersion()
        );
    }
}