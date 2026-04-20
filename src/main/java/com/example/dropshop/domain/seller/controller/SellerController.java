package com.example.dropshop.domain.seller.controller;

import com.example.dropshop.domain.seller.dto.request.SellerApplyRequest;
import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import com.example.dropshop.domain.seller.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // 표준 인터페이스
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/apply")
    public ResponseEntity<SellerResponse> applySeller(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SellerApplyRequest request) {

        // userDetails.getUsername()은 보통 로그인 시 사용한 이메일입니다.
        return ResponseEntity.ok(sellerService.applySeller(userDetails.getUsername(), request));
    }

    @GetMapping("/me/status")
    public ResponseEntity<SellerResponse> getMyStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sellerService.getMySellerStatus(userDetails.getUsername()));
    }
}