package com.example.dropshop.domain.terms.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 약관 제목 (예: 서비스 이용약관)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 약관 상세 내용

    @Column(nullable = false)
    private Boolean isRequired; // 필수 여부

    @Column(nullable = false)
    private String version; // 버전 정보 (예: v1.0)

    @Builder
    public Terms(String title, String content, Boolean isRequired, String version) {
        this.title = title;
        this.content = content;
        this.isRequired = isRequired;
        this.version = version;
    }
}