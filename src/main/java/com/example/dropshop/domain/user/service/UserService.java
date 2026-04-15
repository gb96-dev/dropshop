package com.example.dropshop.domain.user.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.user.dto.request.SignupRequest;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 비즈니스 로직.
     */
    @Transactional
    public void signup(SignupRequest request) {
        // 1. 중복 이메일 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ServiceException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 정적 팩토리 메서드를 사용하여 엔티티 생성
        // 기존의 User.builder()... 대신 사용합니다.
        User user = User.signup(
                request.getEmail(),
                encodedPassword,
                request.getNickname()
        );

        // 4. 저장
        userRepository.save(user);
    }
}