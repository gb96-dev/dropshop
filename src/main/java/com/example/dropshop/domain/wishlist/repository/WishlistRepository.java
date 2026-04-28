package com.example.dropshop.domain.wishlist.repository;

import com.example.dropshop.domain.wishlist.entity.Wishlist;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 찜 JPA 리포지토리.
 */
public interface WishlistRepository extends JpaRepository<Wishlist, Long>, WishlistRepositoryCustom{

  boolean existsByUserIdAndDropId(Long userId, Long dropId);

  void deleteByUserIdAndDropId(Long userId, Long dropId);

  boolean existsByDropId(Long dropId);

  List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
