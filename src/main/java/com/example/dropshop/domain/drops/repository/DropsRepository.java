package com.example.dropshop.domain.drops.repository;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.entity.DropStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DropsRepository extends JpaRepository<Drops, Long> {

    List<Drops> findAllByProduct_IdOrderByStartAtDesc(Long productId);

    List<Drops> findAllByStatusOrderByStartAtAsc(DropStatus status);

    Optional<Drops> findByProduct_IdAndStatus(Long productId, DropStatus status);

    boolean existsByProduct_IdAndStatusIn(Long productId, Collection<DropStatus> statuses);

    List<Drops> findAllByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            DropStatus status,
            LocalDateTime startAt,
            LocalDateTime endAt
    );
}

