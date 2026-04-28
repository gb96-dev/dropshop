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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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

        // ✅ BCrypt 72바이트 제한 해결: SHA-256 해싱 적용
        // 기존 토큰 삭제 후 새 토큰 저장 (명시적 단일 세션 유지)
        refreshTokenRepository.deleteByEmail(user.getEmail());
        String hashedToken = hashToken(refreshToken);
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

        if (!savedToken.getHashedToken().equals(hashToken(refreshToken))) {
            throw new RuntimeException("토큰 정보가 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return jwtUtil.createAccessToken(email, user.getRole().name());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해싱 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}