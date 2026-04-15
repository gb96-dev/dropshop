package com.example.dropshop.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 권한과 그에 대한 설명을 관리하는 Enum입니다.
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {

    USER("일반 사용자"),
    SELLER("판매자"),
    ADMIN("관리자");

    // 권한에 대한 설명을 저장할 필드
    private final String description;
}