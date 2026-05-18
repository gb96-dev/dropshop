package com.example.dropshop.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailUpdateRequest {

  @NotBlank(message = "새 이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String newEmail;

  public EmailUpdateRequest(String newEmail) {
    this.newEmail = newEmail;
  }
}
