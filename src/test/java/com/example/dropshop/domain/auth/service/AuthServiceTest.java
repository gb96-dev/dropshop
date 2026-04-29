package com.example.dropshop.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.jwt.JwtUtil;
import com.example.dropshop.domain.auth.dto.request.LoginRequest;
import com.example.dropshop.domain.auth.dto.response.TokenResponse;
import com.example.dropshop.domain.auth.entity.RefreshToken;
import com.example.dropshop.domain.auth.exception.AuthException;
import com.example.dropshop.domain.auth.repository.RefreshTokenRepository;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.enums.UserRole;
import com.example.dropshop.domain.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password123!");

        User user = mock(User.class);
        given(user.getEmail()).willReturn("test@test.com");
        given(user.getPassword()).willReturn("encodedPassword");
        given(user.getRole()).willReturn(UserRole.USER);

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtUtil.createAccessToken(anyString(), anyString())).willReturn("mock-access-token");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("mock-refresh-token");
        given(refreshTokenRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when
        TokenResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일이면 INVALID_CREDENTIALS 예외 발생")
    void login_fail_userNotFound() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest("none@test.com", "pw")))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치이면 INVALID_CREDENTIALS 예외 발생")
    void login_fail_passwordMismatch() {
        // given
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedPassword");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest("test@test.com", "wrong")))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    // -------------------------------------------------------------------------
    // refresh
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("토큰 갱신 성공")
    void refresh_success() {
        // given
        String email = "test@test.com";
        String rawToken = "valid-refresh-token";

        given(jwtUtil.validateToken(rawToken)).willReturn(true);
        given(jwtUtil.getEmail(rawToken)).willReturn(email);

        RefreshToken savedToken = mock(RefreshToken.class);
        given(savedToken.getHashedToken()).willReturn(hashToken(rawToken));
        given(refreshTokenRepository.findByEmail(email)).willReturn(Optional.of(savedToken));

        User user = mock(User.class);
        given(user.getRole()).willReturn(UserRole.USER);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(jwtUtil.createAccessToken(anyString(), anyString())).willReturn("new-access-token");

        // when
        String newAccessToken = authService.refresh(rawToken);

        // then
        assertThat(newAccessToken).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰이면 INVALID_TOKEN 예외 발생")
    void refresh_fail_invalidToken() {
        // given
        given(jwtUtil.validateToken(anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refresh("bad-token"))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_TOKEN));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 저장된 토큰이 없으면 TOKEN_NOT_FOUND 예외 발생")
    void refresh_fail_tokenNotFound() {
        // given
        given(jwtUtil.validateToken(anyString())).willReturn(true);
        given(jwtUtil.getEmail(anyString())).willReturn("test@test.com");
        given(refreshTokenRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refresh("some-token"))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(ErrorCode.TOKEN_NOT_FOUND));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 해시 불일치이면 TOKEN_MISMATCH 예외 발생")
    void refresh_fail_tokenMismatch() {
        // given
        given(jwtUtil.validateToken(anyString())).willReturn(true);
        given(jwtUtil.getEmail(anyString())).willReturn("test@test.com");

        RefreshToken savedToken = mock(RefreshToken.class);
        given(savedToken.getHashedToken()).willReturn("completely-different-hash");
        given(refreshTokenRepository.findByEmail(anyString())).willReturn(Optional.of(savedToken));

        // when & then
        assertThatThrownBy(() -> authService.refresh("some-token"))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(ErrorCode.TOKEN_MISMATCH));
    }

    // -------------------------------------------------------------------------
    // helper: AuthService 내부 hashToken과 동일한 로직
    // -------------------------------------------------------------------------
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
