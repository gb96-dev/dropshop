package com.example.dropshop.domain.user.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.user.dto.SignupRequest;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.entity.UserRole;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 비즈니스 로직을 담당하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입을 처리합니다.
     * SignupRequest DTO를 통해 데이터를 전달받습니다.
     */
    @Transactional
    public void signup(SignupRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ServiceException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 유저 객체 생성 (Builder 사용)
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(UserRole.USER) // 기본 권한 설정
                .build();

        // 4. 저장 (이 부분이 빠지면 안 됩니다!)
        userRepository.save(user);
    }

    /**
     * 회원 탈퇴를 처리합니다 (Soft Delete).
     * status를 DELETED로 변경하여 7일 후 배치가 처리하도록 합니다.
     */
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        user.withdraw();
    }
}