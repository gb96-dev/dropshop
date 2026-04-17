package com.example.dropshop.domain.user.service;

import com.example.dropshop.domain.user.dto.request.PasswordUpdateRequest;
import com.example.dropshop.domain.user.dto.request.SignupRequest;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.signup(
                request.getEmail(),
                encodedPassword,
                request.getNickname()
        );

        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("기존 비밀번호가 일치하지 않습니다.");
        }

        // 엔티티의 메서드 이름인 changePassword로 호출!
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void withdraw(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}