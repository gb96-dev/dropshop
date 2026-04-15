package com.example.dropshop.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 상태와 그에 대한 설명을 관리하는 Enum입니다.
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {

    ACTIVE("활동 중"),
    SLEEP("휴면 상태"),
    DELETED("탈퇴됨");

    // 상태에 대한 설명을 저장할 필드
    private final String description;
}