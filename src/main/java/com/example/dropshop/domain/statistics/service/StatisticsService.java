package com.example.dropshop.domain.statistics.service;

import com.example.dropshop.domain.statistics.dto.response.CategorySalesResponse;
import com.example.dropshop.domain.statistics.dto.response.PopularProductResponse;
import com.example.dropshop.domain.statistics.dto.response.SalesTrendResponse;
import com.example.dropshop.domain.statistics.repository.StatisticsRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통계 서비스.
 * sellerId 가 null 이면 전체 집계(관리자용), 있으면 해당 판매자만(판매자용).
 * from/to 가 null 이면 최근 30일 기본값 적용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    /**
     * 날짜별 판매 추이 조회.
     *
     * @param from     조회 시작 일시 (null 이면 최근 30일)
     * @param to       조회 종료 일시 (null 이면 현재)
     * @param sellerId 판매자 User.id (null 이면 전체 조회)
     */
    public List<SalesTrendResponse> getSalesTrend(LocalDateTime from, LocalDateTime to,
                                                   Long sellerId) {
        LocalDateTime[] range = resolveRange(from, to);
        return statisticsRepository.findSalesTrend(range[0], range[1], sellerId);
    }

    /**
     * 카테고리별 판매 조회.
     *
     * @param from     조회 시작 일시 (null 이면 최근 30일)
     * @param to       조회 종료 일시 (null 이면 현재)
     * @param sellerId 판매자 User.id (null 이면 전체 조회)
     */
    public List<CategorySalesResponse> getCategorySales(LocalDateTime from, LocalDateTime to,
                                                         Long sellerId) {
        LocalDateTime[] range = resolveRange(from, to);
        return statisticsRepository.findCategorySales(range[0], range[1], sellerId);
    }

    /**
     * 인기 상품 조회 (판매량 기준).
     *
     * @param from     조회 시작 일시 (null 이면 최근 30일)
     * @param to       조회 종료 일시 (null 이면 현재)
     * @param sellerId 판매자 User.id (null 이면 전체 조회)
     * @param limit    조회 건수 (0 이하이면 기본값 10)
     */
    public List<PopularProductResponse> getPopularProducts(LocalDateTime from, LocalDateTime to,
                                                            Long sellerId, int limit) {
        LocalDateTime[] range = resolveRange(from, to);
        int resolvedLimit = limit <= 0 ? 10 : limit;
        return statisticsRepository.findPopularProducts(range[0], range[1], sellerId, resolvedLimit);
    }

    /**
     * from/to 기본값 처리: null 이면 최근 30일.
     */
    private LocalDateTime[] resolveRange(LocalDateTime from, LocalDateTime to) {
        LocalDateTime resolvedTo = (to != null) ? to : LocalDateTime.now();
        LocalDateTime resolvedFrom = (from != null) ? from : resolvedTo.minusDays(30);
        return new LocalDateTime[]{resolvedFrom, resolvedTo};
    }
}
