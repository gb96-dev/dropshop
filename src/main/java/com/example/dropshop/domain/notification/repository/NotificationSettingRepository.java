package com.example.dropshop.domain.notification.repository;

import com.example.dropshop.domain.notification.entity.NotificationSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 알림 설정 리포지토리.
 */
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

  /**
   * 유저의 알림 설정을 조회한다.
   */
  Optional<NotificationSetting> findByUserId(Long userId);

  /**
   * 판매자의 알림 설정 목록을 조회한다 (특정 셀러를 팔로우하는 유저들).
   */
  List<NotificationSetting> findBySellerId(Long sellerId);

  /**
   * 유저-판매자 쌍의 알림 설정을 조회한다.
   */
  Optional<NotificationSetting> findByUserIdAndSellerId(Long userId, Long sellerId);

  /**
   * 드랍 알림이 활성화된 유저-판매자 설정 목록을 반환한다.
   */
  List<NotificationSetting> findBySellerIdAndIsDropEnabledTrue(Long sellerId);
}
