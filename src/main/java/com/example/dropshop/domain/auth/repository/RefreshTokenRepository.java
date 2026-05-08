package com.example.dropshop.domain.auth.repository;

import com.example.dropshop.domain.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByEmail(String email);

  void deleteByEmail(String email);
}
