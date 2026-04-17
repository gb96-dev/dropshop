package com.example.dropshop.domain.auth.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.auth.dto.request.LoginRequest;
import com.example.dropshop.domain.auth.dto.response.TokenResponse;
import com.example.dropshop.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse token = authService.login(request);

        // RefreshToken 쿠키 설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax") // CSRF 방지
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok(token.getAccessToken());
    }

    @PostMapping("/refresh")
    public ApiResponse<String> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken // required = false 추가
    ) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("리프레시 토큰이 쿠키에 없습니다.");
        }
        String newAccessToken = authService.refresh(refreshToken);
        return ApiResponse.ok(newAccessToken);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        // 쿠키 삭제 처리
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.ok();
    }
}