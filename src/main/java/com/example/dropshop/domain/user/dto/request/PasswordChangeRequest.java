package com.example.dropshop.domain.user.dto.request;

import lombok.Getter;

/** 비밀번호 변경 요청 DTO. */
@Getter
public class PasswordChangeRequest {
  private String currentPassword;
  private String newPassword;
}
