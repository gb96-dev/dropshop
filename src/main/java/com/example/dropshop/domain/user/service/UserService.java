package com.example.dropshop.domain.user.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.common.kafka.producer.EventKafkaProducer;
import com.example.dropshop.domain.user.dto.request.PasswordUpdateRequest;
import com.example.dropshop.domain.user.dto.request.SignupRequest;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.event.UserSignupEvent;
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
    private final EventKafkaProducer eventKafkaProducer;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ServiceException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.signup(request.getEmail(), encodedPassword, request.getNickname());
        userRepository.save(user);

        // Kafka 회원가입 이벤트 발행
        eventKafkaProducer.publishUserSignup(UserSignupEvent.of(user.getEmail()));
    }

    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ServiceException(ErrorCode.PASSWORD_MISMATCH);
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void withdraw(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        user.withdraw();
        userRepository.delete(user);
    }
}