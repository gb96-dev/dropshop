package com.example.dropshop.domain.seller.repository;

import com.example.dropshop.domain.seller.entity.Seller;
import com.example.dropshop.domain.seller.enums.SellerStatus;
import com.example.dropshop.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {
  boolean existsByBusinessNo(String businessNo);

  Optional<Seller> findByUser(User user);

  List<Seller> findAllByStatus(SellerStatus status);
}
