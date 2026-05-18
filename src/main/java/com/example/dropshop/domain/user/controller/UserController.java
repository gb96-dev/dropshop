package com.example.dropshop.domain.user.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.user.dto.request.EmailUpdateRequest;
import com.example.dropshop.domain.user.dto.request.PasswordUpdateRequest;
import com.example.dropshop.domain.user.dto.request.SignupRequest;
import com.example.dropshop.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

  @PatchMapping("/email")
  @Operation(
      summary = "이메일 변경",
      description =
          "로그인한 사용자의 이메일을 변경합니다. "
              + "JWT subject가 email이므로 변경 즉시 기존 Refresh Token 삭제 및 Access Token 블랙리스트 처리됩니다.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<Void> updateEmail(
      @AuthenticationPrincipal String email,
      @Valid @RequestBody EmailUpdateRequest request,
      HttpServletRequest httpRequest) {
    String accessToken = resolveToken(httpRequest);
    userService.changeEmail(email, request.getNewEmail(), accessToken);
    return ApiResponse.ok();
  }

  @DeleteMapping("/withdraw")
  @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 탈퇴 처리합니다.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<Void> withdraw(@AuthenticationPrincipal String email) {
    userService.withdraw(email);
    return ApiResponse.ok();
  }

  private String resolveToken(HttpServletRequest request) {
    String bearer = request.getHeader("Authorization");
    if (bearer != null && bearer.startsWith("Bearer ")) {
      return bearer.substring(7);
    }
    return "";
  }
}
