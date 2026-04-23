package com.example.dropshop.domain.drops.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.drops.dto.response.DropListItemResponse;
import com.example.dropshop.domain.drops.dto.response.DropResponse;
import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.enums.DropsStatus;
import com.example.dropshop.domain.drops.exception.DropsException;
import com.example.dropshop.domain.drops.repository.DropsRepository;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 드랍 조회 서비스 (읽기 최적화).
 * 기술 설명.
 * - @Transactional(readOnly = true): 읽기 전용으로 플러시 모드 NEVER, 스냅샷 비활성화 최적화
 * - @EntityGraph: N+1 쿼리 방지, Product를 함께 로드
 * - Page<T>: 페이징, 정렬, 총 개수 자동 계산
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DropsQueryService {

  private static final Set<DropsStatus> PUBLIC_VISIBLE_STATUSES =
      DropsService.PUBLIC_VISIBLE_STATUSES;

  private final DropsRepository dropsRepository;

  /**
   * 공개 드롭 목록을 조회한다.
   * 도메인 규칙.
   * - SCHEDULED: 예정 드롭 (진행 전)
   * - ACTIVE: 진행 중 드롭
   * - FINISHED: 종료된 드롭
   * 기술.
   * - @EntityGraph로 Product 즉시 로드 (N+1 방지)
   * - Page 객체로 페이징/정렬/총 개수 자동 처리
   */
  public Page<DropListItemResponse> findPublicDrops(DropsStatus status, Pageable pageable) {
    Set<DropsStatus> filterStatuses = status == null
        ? PUBLIC_VISIBLE_STATUSES
        : EnumSet.of(status);

    Page<Drops> drops = dropsRepository.findAllByStatusIn(filterStatuses, pageable);
    return drops.map(DropListItemResponse::from);
  }

  /**
   * 드롭 상세 조회.
   * 도메인 규칙.
   * - SCHEDULED, ACTIVE, FINISHED만 공개 조회 가능
   * - useQueue 여부 표시 (규칙 4번: 선착순 드롭 구분)
   */
  public DropResponse findPublicDropDetail(Long dropId) {
    Drops drops = dropsRepository.findOneByIdAndStatusIn(dropId, PUBLIC_VISIBLE_STATUSES)
        .orElseThrow(() -> new DropsException(ErrorCode.DROP_NOT_FOUND));

    return DropResponse.from(drops);
  }

  /**
   * 판매자 본인 드롭 목록 조회.
   * 도메인 규칙.
   * - 판매자는 본인이 등록한 상품의 드롭만 조회 가능
   * - 모든 상태(SCHEDULED, ACTIVE, FINISHED)의 드롭 조회 가능
   */
  public Page<DropListItemResponse> findSellerDrops(
      Long sellerId,
      Pageable pageable
  ) {
    Page<Drops> drops = dropsRepository.findSellerDropsBySellerId(
        sellerId,
        pageable
    );
    return drops.map(DropListItemResponse::from);
  }

  /**
   * 특정 상품의 드롭 이력 조회.
   * 도메인 규칙.
   * - 공개 상태 드롭만 조회 가능
   * - 판매된 수량(totalStock - remainStock) 포함
   */
  public Page<DropListItemResponse> findDropsByProduct(
      Long productId,
      Pageable pageable
  ) {
    Page<Drops> drops = dropsRepository.findAllByProductIdAndStatusIn(
        productId,
        PUBLIC_VISIBLE_STATUSES,
        pageable
    );

    return drops.map(DropListItemResponse::from);
  }
}




