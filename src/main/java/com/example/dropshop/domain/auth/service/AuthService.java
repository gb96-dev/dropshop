package com.example.dropshop.domain.auth.service;

import com.example.dropshop.domain.auth.dto.request.LoginRequest;
import com.example.dropshop.domain.auth.dto.response.TokenResponse;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import com.example.dropshop.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        // TODO: passwordEncoder 비교 필수
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호 틀림");
        }

        String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        return new TokenResponse(accessToken, refreshToken);
    }

    public String refresh(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("RefreshToken 만료");
        }

        String email = jwtUtil.getEmail(refreshToken);

        return jwtUtil.createAccessToken(email, "USER");
    }
}
