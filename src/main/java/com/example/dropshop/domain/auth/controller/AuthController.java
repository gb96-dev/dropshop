package com.example.dropshop.domain.auth.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.auth.dto.request.LoginRequest;
import com.example.dropshop.domain.auth.dto.response.TokenResponse;
import com.example.dropshop.domain.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {

        TokenResponse token = authService.login(request);

        Cookie cookie = new Cookie("refreshToken", token.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        return ApiResponse.ok(token.getAccessToken());
    }

    @PostMapping("/refresh")
    public ApiResponse<String> refresh(@CookieValue("refreshToken") String refreshToken) {
        String newAccessToken = authService.refresh(refreshToken);
        return ApiResponse.ok(newAccessToken);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);

        return ApiResponse.ok();
    }
}
