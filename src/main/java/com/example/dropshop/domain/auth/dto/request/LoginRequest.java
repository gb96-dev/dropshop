package com.example.dropshop.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequest {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  @Schema(description = "로그인 이메일", example = "seller-test@dropshop.com")
  private String email;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Schema(description = "로그인 비밀번호", example = "Password123!")
  private String password;

  public LoginRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
