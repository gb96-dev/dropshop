package com.example.dropshop.domain.user.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.user.dto.request.PasswordUpdateRequest;
import com.example.dropshop.domain.user.dto.request.SignupRequest;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.event.UserSignupEvent;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ServiceException(ErrorCode.DUPLICATE_EMAIL);
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    User user = User.signup(request.getEmail(), encodedPassword, request.getNickname());
    userRepository.save(user);

    // DB 커밋 이후에만 Kafka 이벤트가 발행되도록 Spring 내부 이벤트로 전달.
    // 실제 Kafka 발행은 UserSignupEventListener(AFTER_COMMIT)에서 수행된다.
    eventPublisher.publishEvent(UserSignupEvent.of(user.getEmail()));
  }

  @Transactional
  public void updatePassword(String email, PasswordUpdateRequest request) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new ServiceException(ErrorCode.PASSWORD_MISMATCH);
    }

    user.changePassword(passwordEncoder.encode(request.getNewPassword()));
  }

  @Transactional
  public void withdraw(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

    user.withdraw();
    userRepository.delete(user);
  }
}
