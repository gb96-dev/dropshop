package com.example.dropshop.domain.seller.service;

import com.example.dropshop.domain.seller.dto.request.SellerApplyRequest;
import com.example.dropshop.domain.seller.dto.request.SellerUpdateRequest;
import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import com.example.dropshop.domain.seller.entity.Seller;
import com.example.dropshop.domain.seller.repository.SellerRepository;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    @Transactional
    public SellerResponse applySeller(String email, SellerApplyRequest request) {
        // 1. 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 사업자 번호 중복 체크
        if (sellerRepository.existsByBusinessNo(request.getBusinessNo())) {
            throw new IllegalArgumentException("이미 등록된 사업자 번호입니다.");
        }

        // 3. 이미 신청했는지 체크
        if (sellerRepository.findByUser(user).isPresent()) {
            throw new IllegalArgumentException("이미 판매자 신청 내역이 존재합니다.");
        }

        // 4. 저장
        Seller seller = Seller.builder()
                .user(user)
                .businessNo(request.getBusinessNo())
                .brandName(request.getBrandName())
                .brandLogo(request.getBrandLogo())
                .accountInfo(request.getAccountInfo())
                .build();

        return new SellerResponse(sellerRepository.save(seller));
    }

    public SellerResponse getMySellerStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("판매자 신청 내역이 없습니다."));
        return new SellerResponse(seller);
    }

    @Transactional
    public SellerResponse updateSeller(String email, SellerUpdateRequest request) {
        // 1. 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 해당 유저의 판매자 정보 조회
        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        // 3. 정보 업데이트 (엔티티 내부에 만든 updateInfo 메서드 활용)
        seller.updateInfo(
                request.getBrandName(),
                request.getBrandLogo(),
                request.getAccountInfo()
        );

        return new SellerResponse(seller);
    }
}