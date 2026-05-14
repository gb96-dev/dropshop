package com.example.dropshop.domain.queue.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.entity.QueueToken;
import com.example.dropshop.domain.queue.enums.QueueStatus;
import com.example.dropshop.domain.queue.repository.QueueRepository;
import com.example.dropshop.domain.queue.repository.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 대기열 토큰 검증 서비스. */
@Service
@RequiredArgsConstructor
public class QueueTokenValidationService {

  private final QueueRepository queueRepository;
  private final QueueTokenRepository queueTokenRepository;

  /**
   * 대기열 토큰 검증 with drop.
   *
   * @param dropId 드랍 아이디.
   * @param userId 유저 아이디.
   * @return 리턴.
   */
  public boolean validationQueueTokenWithDrop(Long dropId, Long userId) {
    Queue queue =
        queueRepository
            .findByDropIdAndUserId(dropId, userId)
            .orElseThrow(() -> new ServiceException(ErrorCode.QUEUE_NOT_FOUND));

    if (!(queue.getStatus().equals(QueueStatus.READY)
        || queue.getStatus().equals(QueueStatus.ENTERED))) {
      return false;
    }

    return true;
  }

  /**
   * 대기열 토큰 검증 with order.
   *
   * @param token 대기열 토큰.
   * @return 리턴.
   */
  public boolean validationQueueTokenWithOrder(String token, Long userId) {
    QueueToken queueToken =
        queueTokenRepository
            .findByQueueToken(token)
            .orElseThrow(() -> new ServiceException(ErrorCode.QUEUE_TOKEN_NOT_FOUND));

    Queue queue = queueToken.getQueue();

    if (!queue.getUserId().equals(userId)) {
      return false;
    }

    if (!(queue.getStatus().equals(QueueStatus.READY)
        || queue.getStatus().equals(QueueStatus.ENTERED))) {
      return false;
    }

    return true;
  }
}
