package com.example.dropshop.domain.admin.service;

import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import com.example.dropshop.domain.seller.entity.Seller;
import com.example.dropshop.domain.seller.enums.SellerStatus;
import com.example.dropshop.domain.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자용 판매자 관리 서비스.
 * - 전체 판매자 목록 조회
 * - 승인 대기(PENDING) 판매자 목록 조회
 * - 판매자 승인 (PENDING → APPROVED)
 * - 판매자 정지 (APPROVED → SUSPENDED)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSellerService {

    private final SellerRepository sellerRepository;

    /**
     * 전체 판매자 목록 조회.
     */
    public List<SellerResponse> getAllSellers() {
        return sellerRepository.findAll().stream()
                .map(SellerResponse::new)
                .toList();
    }

    /**
     * 승인 대기중(PENDING)인 판매자 목록 조회.
     */
    public List<SellerResponse> getPendingSellers() {
        return sellerRepository.findAllByStatus(SellerStatus.PENDING).stream()
                .map(SellerResponse::new)
                .toList();
    }

    /**
     * 판매자 승인.
     */
    @Transactional
    public SellerResponse approveSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));

        if (seller.getStatus() != SellerStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태인 판매자만 승인할 수 있습니다.");
        }

        seller.approve();
        return new SellerResponse(seller);
    }

    /**
     * 판매자 정지.
     */
    @Transactional
    public SellerResponse suspendSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));

        if (seller.getStatus() != SellerStatus.APPROVED) {
            throw new IllegalStateException("승인된 판매자만 정지할 수 있습니다.");
        }

        seller.suspend();
        return new SellerResponse(seller);
    }
}
