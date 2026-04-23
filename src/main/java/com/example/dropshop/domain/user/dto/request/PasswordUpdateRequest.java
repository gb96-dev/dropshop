package com.example.dropshop.domain.user.dto.request;

import lombok.Getter;

/**
 * 비밀번호 변경 요청을 위한 DTO입니다.
 */
@Getter
public class PasswordUpdateRequest {
    private String oldPassword; // 기존 비밀번호
    private String newPassword; // 새 비밀번호
}