package com.example.dropshop.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 사용자의 권한을 정의하는 Enum입니다. */
@Getter
@RequiredArgsConstructor
public enum UserRole {
  USER("ROLE_USER"),
  SELLER("ROLE_SELLER"),
  ADMIN("ROLE_ADMIN");

  private final String authority;
}
