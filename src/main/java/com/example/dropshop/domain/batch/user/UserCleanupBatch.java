package com.example.dropshop.domain.batch.user;

import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.enums.UserStatus;
import com.example.dropshop.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 탈퇴 사용자 정리 배치. */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupBatch {

  private final UserRepository userRepository;

  /** 탈퇴 후 7일 지난 사용자 물리 삭제. */
  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void cleanupDeletedUsers() {

    LocalDateTime threshold = LocalDateTime.now().minusDays(7);

    List<User> targets =
        userRepository.findAll().stream()
            .filter(user -> user.getStatus() == UserStatus.DELETED)
            .filter(user -> user.getDeletedAt() != null)
            .filter(user -> user.getDeletedAt().isBefore(threshold))
            .toList();

    if (targets.isEmpty()) {
      log.info("[UserCleanupBatch] 삭제 대상 없음");
      return;
    }

    log.info("[UserCleanupBatch] 삭제 대상 수: {}", targets.size());

    userRepository.deleteAll(targets);

    log.info("[UserCleanupBatch] 사용자 삭제 완료");
  }
}
