package com.example.dropshop.domain.user.service;

import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 도메인 파사드 서비스.
 */
@Service
@RequiredArgsConstructor
public class UserFacadeService {

  private final UserRepository userRepository;

  /**
   * 사용자 ID로 사용자 정보를 조회한다.
   */
  @Transactional(readOnly = true)
  public Optional<User> findById(Long userId) {
    return userRepository.findById(userId);
  }

  /**
   * 이메일로 사용자 정보를 조회한다.
   *
   * @param email 사용자 이메일
   * @return 사용자 정보(Optional)
   */
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  /**
   * 이메일로 사용자 ID를 조회한다.
   *
   * @param email 사용자 이메일
   * @return 사용자 ID
   */
  @Transactional(readOnly = true)
  public Long getUserIdByEmail(String email) {
    return findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다."))
        .getId();
  }
  /**
   * 이메일로 사용자 정보를 조회한다.
   * SellerAuthResolver(37라인)에서 호출하는 메서드입니다.
   */
  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. email: " + email));
  }
}
