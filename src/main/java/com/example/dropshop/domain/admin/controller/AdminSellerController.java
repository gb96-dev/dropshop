package com.example.dropshop.domain.admin.controller;

import com.example.dropshop.domain.admin.service.AdminSellerService;
import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자용 판매자 관리 API. SecurityConfig 에서 /api/admin/sellers/** 는 ROLE_ADMIN 만 접근 가능하도록 설정되어 있다. */
@RestController
@RequestMapping("/api/admin/sellers")
@RequiredArgsConstructor
public class AdminSellerController {

  private final AdminSellerService adminSellerService;

  /** 전체 판매자 목록 조회. GET /api/admin/sellers */
  @GetMapping
  public ResponseEntity<List<SellerResponse>> getAllSellers() {
    return ResponseEntity.ok(adminSellerService.getAllSellers());
  }

  /** 승인 대기중(PENDING)인 판매자 목록 조회. GET /api/admin/sellers/pending */
  @GetMapping("/pending")
  public ResponseEntity<List<SellerResponse>> getPendingSellers() {
    return ResponseEntity.ok(adminSellerService.getPendingSellers());
  }

  /** 판매자 승인. PATCH /api/admin/sellers/{sellerId}/approve */
  @PatchMapping("/{sellerId}/approve")
  public ResponseEntity<SellerResponse> approveSeller(@PathVariable Long sellerId) {
    return ResponseEntity.ok(adminSellerService.approveSeller(sellerId));
  }

  /** 판매자 정지. PATCH /api/admin/sellers/{sellerId}/suspend */
  @PatchMapping("/{sellerId}/suspend")
  public ResponseEntity<SellerResponse> suspendSeller(@PathVariable Long sellerId) {
    return ResponseEntity.ok(adminSellerService.suspendSeller(sellerId));
  }
}
