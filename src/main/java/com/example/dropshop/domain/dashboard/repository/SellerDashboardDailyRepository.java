package com.example.dropshop.domain.dashboard.repository;

import com.example.dropshop.domain.dashboard.entity.SellerDashboardDaily;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 판매자 일별 대시보드 집계 레포지토리. */
public interface SellerDashboardDailyRepository extends JpaRepository<SellerDashboardDaily, Long> {

  Optional<SellerDashboardDaily> findBySellerIdAndStatDate(Long sellerId, LocalDate statDate);

  List<SellerDashboardDaily> findAllBySellerIdAndStatDateBetween(
      Long sellerId, LocalDate from, LocalDate to);
}
