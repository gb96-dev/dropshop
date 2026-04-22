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
@RequiredArgsConstructor // final 필드에 대한 생성자만 생성 (튜터님 피드백 반영)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository; // 리프레시 토큰 저장소 추가
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 틀렸습니다.");
        }

        // 1. 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        // 2. 리프레시 토큰 저장 (기존 토큰이 있다면 덮어쓰기)
        RefreshToken refreshTokenEntity = new RefreshToken(user.getEmail(), refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public String refresh(String refreshToken) {
        // 1. 리프레시 토큰 자체의 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("RefreshToken이 유효하지 않습니다.");
        }

        String email = jwtUtil.getEmail(refreshToken);

        // 2. DB에 저장된 토큰과 일치하는지 확인 (보안 강화)
        RefreshToken savedToken = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그아웃된 사용자이거나 토큰이 없습니다."));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new RuntimeException("토큰 정보가 일치하지 않습니다.");
        }

        // 3. 유저 정보 조회 및 새 액세스 토큰 발급
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return jwtUtil.createAccessToken(email, user.getRole().name());
    }
}