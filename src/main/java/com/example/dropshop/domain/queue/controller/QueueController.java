package com.example.dropshop.domain.queue.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.queue.dto.response.ThreadHoldResponse;
import com.example.dropshop.domain.queue.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 대기열 컨트롤러. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/queues")
@Tag(name = "Queue", description = "대기열 API")
@SecurityRequirement(name = "bearerAuth")
public class QueueController {

  private final QueueService queueService;

  /**
   * 대기열 여부 결정
   *
   * @param dropId 드랍 아이디.
   * @param userEmail 유저 이메일.
   */
  @PostMapping
  @Operation(summary = "대기열 진입 여부 판단", description = "드랍 기준으로 사용자의 대기열 진입 여부를 판단합니다.")
  public ResponseEntity<ApiResponse<ThreadHoldResponse>> decideQueue(
      @Parameter(description = "대기열 진입을 시도할 드랍 ID", example = "1")
          @RequestParam(name = "dropId")
          Long dropId,
      @AuthenticationPrincipal String userEmail) {
    ThreadHoldResponse response = queueService.decideQueue(dropId, userEmail);

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
  }
}
