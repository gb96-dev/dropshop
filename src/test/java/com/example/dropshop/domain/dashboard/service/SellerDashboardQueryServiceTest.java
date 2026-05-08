package com.example.dropshop.domain.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.dropshop.domain.dashboard.dto.response.SellerDashboardOrderItemResponse;
import com.example.dropshop.domain.dashboard.dto.response.SellerDashboardSummaryResponse;
import com.example.dropshop.domain.dashboard.entity.SellerDashboardDaily;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardDailyRepository;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardMetricsRepository;
import com.example.dropshop.domain.dashboard.repository.SellerDashboardOrderItemView;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class SellerDashboardQueryServiceTest {

  @Mock
  private SellerDashboardDailyRepository sellerDashboardDailyRepository;

  @Mock
  private SellerDashboardMetricsRepository sellerDashboardMetricsRepository;

  @InjectMocks
  private SellerDashboardQueryService sellerDashboardQueryService;

  @Test
  @DisplayName("대시보드 요약은 일별 테이블과 distinct buyer 수를 합쳐 반환한다")
  void getSummary_success() {
    LocalDate from = LocalDate.of(2026, 5, 1);
    LocalDate to = LocalDate.of(2026, 5, 7);

    given(sellerDashboardDailyRepository.findAllBySellerIdAndStatDateBetween(1L, from, to))
        .willReturn(List.of(
            SellerDashboardDaily.create(1L, from, 2L, 3L, new BigDecimal("158000"), 2L),
            SellerDashboardDaily.create(1L, from.plusDays(1), 1L, 1L, new BigDecimal("79000"), 1L)
        ));
    given(sellerDashboardMetricsRepository.countDistinctBuyers(1L, from, to)).willReturn(2L);

    SellerDashboardSummaryResponse result =
        sellerDashboardQueryService.getSummary(1L, from, to);

    assertThat(result.paidOrderCount()).isEqualTo(3L);
    assertThat(result.salesQuantity()).isEqualTo(4L);
    assertThat(result.salesAmount()).isEqualByComparingTo("237000");
    assertThat(result.buyerCount()).isEqualTo(2L);
  }

  @Test
  @DisplayName("판매자 주문 내역은 응답 DTO 페이지로 변환된다")
  void getOrderItems_success() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<SellerDashboardOrderItemView> source = new PageImpl<>(
        List.of(new SellerDashboardOrderItemView(
            1L,
            "ORDER-123",
            10L,
            100L,
            "테스트 상품",
            "https://dummy-image",
            1,
            new BigDecimal("79000"),
            OrderStatus.PAID,
            LocalDateTime.of(2026, 5, 7, 10, 0)
        )),
        pageable,
        1
    );
    given(sellerDashboardMetricsRepository.findSellerOrderItems(
        1L,
        OrderStatus.PAID,
        null,
        null,
        pageable
    )).willReturn(source);

    Page<SellerDashboardOrderItemResponse> result = sellerDashboardQueryService.getOrderItems(
        1L,
        OrderStatus.PAID,
        null,
        null,
        pageable
    );

    assertThat(result.getTotalElements()).isEqualTo(1L);
    assertThat(result.getContent().get(0).productName()).isEqualTo("테스트 상품");
    assertThat(result.getContent().get(0).salesAmount()).isEqualByComparingTo("79000");
  }
}
