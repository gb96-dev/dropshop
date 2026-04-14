package com.example.dropshop.domain.wishlist.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

/**
 * 찜 Entity.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "wishlists",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_drop", columnNames = {"user_id", "drop_id"})
    },
    indexes = {
        @Index(name = "idx_drop", columnList = "drop_id")
    }
)
public class Wishlist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  private Long dropId;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  /**
   * 찜 생성자.
   * @param userId 유저 아이디.
   * @param dropId 드랍 아이디.
   */
  public Wishlist(Long userId, Long dropId) {
    this.userId = userId;
    this.dropId = dropId;
  }
}