package com.example.dropshop.domain.user.service;

import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFacadeService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Optional<User> findById(Long userId) {
    return userRepository.findById(userId);
  }

  /**
   * 1. Optional을 반환하는 기본 조회 메서드
   */
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  /**
   * 2. 예외 처리가 포함된 사용자 조회 (SellerAuthResolver 등에서 사용)
   * 메서드 이름을 findByEmailOrThrow로 변경하여 중복을 피하고 명확성을 높였습니다.
   */
  @Transactional(readOnly = true)
  public User findByEmailOrThrow(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. email: " + email));
  }

  @Transactional(readOnly = true)
  public Long getUserIdByEmail(String email) {
    return findByEmailOrThrow(email).getId();
  }
}