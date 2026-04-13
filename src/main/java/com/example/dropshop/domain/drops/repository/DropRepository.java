package com.example.dropshop.domain.drops.repository;

import com.example.dropshop.domain.drops.entity.Drop;
import com.example.dropshop.domain.drops.entity.DropStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DropRepository extends JpaRepository<Drop, Long> {

    List<Drop> findAllByProduct_IdOrderByStartAtDesc(Long productId);

    List<Drop> findAllByStatusOrderByStartAtAsc(DropStatus status);

    Optional<Drop> findByProduct_IdAndStatus(Long productId, DropStatus status);

    boolean existsByProduct_IdAndStatusIn(Long productId, Collection<DropStatus> statuses);

    List<Drop> findAllByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            DropStatus status,
            LocalDateTime startAt,
            LocalDateTime endAt
    );
}

