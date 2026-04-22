package com.example.dropshop.domain.queue.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.enums.DropsStatus;
import com.example.dropshop.domain.drops.repository.DropsRepository;
import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.entity.QueueToken;
import com.example.dropshop.domain.queue.enums.QueueStatus;
import com.example.dropshop.domain.queue.enums.ThreadHoldResult;
import com.example.dropshop.domain.queue.repository.QueueRepository;
import com.example.dropshop.domain.queue.repository.QueueTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

  @Mock
  private QueueRepository queueRepository;

  @Mock
  private QueueTokenRepository queueTokenRepository;

  @Mock
  private DropsRepository dropsRepository;

  @InjectMocks
  private QueueService queueService;

  @Test
  void 드랍이_존재하지_않음() {
    // given
    given(dropsRepository.findById(1L)).willReturn(Optional.empty());

    // when && then
    assertThrows(ServiceException.class,
        () -> queueService.decideQueue(1L, 1L));
  }

  @Test
  void 드랍이_ACTIVE가_아니면_예외() {
    // given
    Drops drop = mock(Drops.class);

    given(drop.getStatus()).willReturn(DropsStatus.FINISHED);

    given(dropsRepository.findById(1L)).willReturn(Optional.of(drop));

    // when && then
    assertThrows(ServiceException.class,
        () -> queueService.decideQueue(1L, 1L));
  }

  @Test
  void 기존_WAITING이면_대기열_DIRECT_반환() {
    // given
    Queue queue = mock(Queue.class);

    Drops activeDrop = mock(Drops.class);

    given(activeDrop.getStatus()).willReturn(DropsStatus.ACTIVE);

    given(dropsRepository.findById(1L)).willReturn(Optional.of(activeDrop));

    given(queueRepository.findByDropIdAndUserIdAndStatusIn(any(), any(), any()))
        .willReturn(List.of(queue));

    given(queue.getStatus()).willReturn(QueueStatus.WAITING);

    given(queue.getEnteredAt()).willReturn(LocalDateTime.now().minusMinutes(5));

    given(queue.getId()).willReturn(10L);

    given(queueRepository.countByDropIdAndStatusAndEnteredAtBefore(any(), any(), any()))
        .willReturn(5L);

    QueueToken mockToken = mock(QueueToken.class);

    given(queueTokenRepository.save(any())).willReturn(mockToken);

    given(mockToken.getCreatedAt()).willReturn(LocalDateTime.now());

    // when
    ThreadHoldResponse response = queueService.decideQueue(1L, 1L);

    // then
    assertEquals(ThreadHoldResult.DIRECT, response.getResult());
    assertEquals(0L, response.getAheadCount());
  }

  @Test
  void 기존_WAITING이면_대기열_QUEUE_반환() {
    // given
    Queue queue = mock(Queue.class);

    Drops activeDrop = mock(Drops.class);

    given(activeDrop.getStatus()).willReturn(DropsStatus.ACTIVE);

    given(dropsRepository.findById(1L)).willReturn(Optional.of(activeDrop));

    given(queueRepository.findByDropIdAndUserIdAndStatusIn(any(), any(), any()))
        .willReturn(List.of(queue));

    given(queue.getStatus()).willReturn(QueueStatus.WAITING);

    given(queue.getEnteredAt()).willReturn(LocalDateTime.now().minusMinutes(5));

    given(queue.getId()).willReturn(10L);

    given(queueRepository.countByDropIdAndStatusAndEnteredAtBefore(any(), any(), any()))
        .willReturn(500L);

    // when
    ThreadHoldResponse response = queueService.decideQueue(1L, 1L);

    // then
    assertEquals(ThreadHoldResult.QUEUE, response.getResult());
    assertEquals(500L, response.getAheadCount());
    assertEquals(200L, response.getEstimatedWaitSeconds());
  }

  @Test
  void 기존_READY이면_바로_입장() {
    // given
    Queue queue = mock(Queue.class);
    QueueToken token = mock(QueueToken.class);
    Drops activeDrop = mock(Drops.class);

    given(activeDrop.getStatus()).willReturn(DropsStatus.ACTIVE);

    given(dropsRepository.findById(1L)).willReturn(Optional.of(activeDrop));

    given(queueRepository.findByDropIdAndUserIdAndStatusIn(any(), any(), any()))
        .willReturn(List.of(queue));

    given(queue.getStatus()).willReturn(QueueStatus.READY);

    given(queue.getId()).willReturn(10L);

    given(queueTokenRepository.findByQueueId(10L))
        .willReturn(Optional.of(token));

    given(token.getQueueToken()).willReturn("testToken");

    given(token.getCreatedAt()).willReturn(LocalDateTime.now());

    // when
    ThreadHoldResponse response = queueService.decideQueue(1L, 1L);

    // then
    assertEquals(ThreadHoldResult.DIRECT, response.getResult());
    assertEquals("testToken", response.getAdmissionToken());
  }

  @Test
  void 신규유저_자리있으면_DIRECT() {
    // given
    Drops activeDrop = mock(Drops.class);

    given(activeDrop.getStatus()).willReturn(DropsStatus.ACTIVE);

    given(dropsRepository.findById(1L)).willReturn(Optional.of(activeDrop));

    given(queueRepository.findByDropIdAndUserIdAndStatusIn(any(), any(), any()))
        .willReturn(List.of());

    Queue queue =  mock(Queue.class);

    given(queueRepository.save(any())).willReturn(queue);

    given(queueRepository.countByDropIdAndStatusIn(any(), any()))
        .willReturn(5L);

    given(queueTokenRepository.save(any()))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    ThreadHoldResponse response = queueService.decideQueue(1L, 1L);

    // then
    assertEquals(ThreadHoldResult.DIRECT, response.getResult());
    assertNotNull(response.getAdmissionToken());
  }

  @Test
  void 신규유저_자리없으면_QUEUE() {
    // given
    Drops activeDrop = mock(Drops.class);

    given(activeDrop.getStatus()).willReturn(DropsStatus.ACTIVE);

    given(dropsRepository.findById(1L)).willReturn(Optional.of(activeDrop));

    given(queueRepository.findByDropIdAndUserIdAndStatusIn(any(), any(), any()))
        .willReturn(List.of());

    Queue queue =  mock(Queue.class);

    given(queueRepository.save(any())).willReturn(queue);

    given(queueRepository.countByDropIdAndStatusIn(any(), any()))
        .willReturn(15L);

//    given(queueTokenRepository.save(any()))
//        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    ThreadHoldResponse response = queueService.decideQueue(1L, 1L);

    // then
    assertEquals(ThreadHoldResult.QUEUE, response.getResult());
    assertTrue(response.getAheadCount() >= 10);
  }
}