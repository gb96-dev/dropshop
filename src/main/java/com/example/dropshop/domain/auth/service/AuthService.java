package com.example.dropshop.domain.auth.service;

import com.example.dropshop.common.jwt.JwtUtil;
import com.example.dropshop.domain.auth.dto.request.LoginRequest;
import com.example.dropshop.domain.auth.dto.response.TokenResponse;
import com.example.dropshop.domain.auth.entity.RefreshToken;
import com.example.dropshop.domain.auth.repository.RefreshTokenRepository;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("정보가 틀렸습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("정보가 틀렸습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        String hashedToken = passwordEncoder.encode(refreshToken);
        RefreshToken refreshTokenEntity = new RefreshToken(user.getEmail(), hashedToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public String refresh(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        String email = jwtUtil.getEmail(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그아웃된 세션입니다."));

        if (!passwordEncoder.matches(refreshToken, savedToken.getHashedToken())) {
            throw new RuntimeException("토큰 정보가 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return jwtUtil.createAccessToken(email, user.getRole().name());
    }
}