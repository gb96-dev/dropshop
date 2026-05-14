package com.example.dropshop.domain.user.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.user.dto.request.PasswordUpdateRequest;
import com.example.dropshop.domain.user.dto.request.SignupRequest;
import com.example.dropshop.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 API")
public class UserController {

  private final UserService userService;

  @PostMapping("/signup")
  @Operation(summary = "회원가입", description = "새로운 일반 회원 계정을 생성합니다.")
  public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
    userService.signup(request);
    return ApiResponse.ok();
  }

  @PatchMapping("/password")
  @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<Void> updatePassword(
      @AuthenticationPrincipal String email, @RequestBody PasswordUpdateRequest request) {
    userService.updatePassword(email, request);
    return ApiResponse.ok();
  }

  @DeleteMapping("/withdraw")
  @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 탈퇴 처리합니다.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<Void> withdraw(@AuthenticationPrincipal String email) {
    userService.withdraw(email);
    return ApiResponse.ok();
  }
}
