package com.example.dropshop.domain.queue.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.example.dropshop.domain.queue.entity.Queue;
import com.example.dropshop.domain.queue.entity.QueueToken;
import com.example.dropshop.domain.queue.repository.QueueRepository;
import com.example.dropshop.domain.queue.repository.QueueTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadyQueueTokenListenerTest {

  @Mock
  private QueueTokenRepository queueTokenRepository;

  @Mock
  private QueueRepository queueRepository;

  @InjectMocks
  private ReadyQueueTokenListener readyQueueTokenListener;

  @Test
  void 큐토큰이_없으면_예외없이_건너뛴다() {
    ThreadHoldResponse response = ThreadHoldResponse.expire(1L, 10L);

    when(queueTokenRepository.findByQueueId(10L)).thenReturn(Optional.empty());

    readyQueueTokenListener.consume(response);

    verify(queueRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void 큐가_없으면_예외없이_건너뛴다() {
    ThreadHoldResponse response = ThreadHoldResponse.expire(1L, 10L);
    QueueToken token = mock(QueueToken.class);

    when(token.getId()).thenReturn(99L);
    when(queueTokenRepository.findByQueueId(10L)).thenReturn(Optional.of(token));
    when(queueRepository.findByQueue(token)).thenReturn(Optional.empty());

    readyQueueTokenListener.consume(response);

    verify(queueRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void 큐가_있으면_만료처리한다() {
    ThreadHoldResponse response = ThreadHoldResponse.expire(1L, 10L);
    Queue queue = new Queue(1L, 1L);
    QueueToken token = new QueueToken("token", queue);

    when(queueTokenRepository.findByQueueId(10L)).thenReturn(Optional.of(token));
    when(queueRepository.findByQueue(token)).thenReturn(Optional.of(queue));

    readyQueueTokenListener.consume(response);

    verify(queueRepository).save(queue);
  }
}
