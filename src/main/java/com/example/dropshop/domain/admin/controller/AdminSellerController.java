package com.example.dropshop.domain.admin.controller;

import com.example.dropshop.domain.admin.service.AdminSellerService;
import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Seller", description = "관리자 판매자 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class AdminSellerController {

  private final AdminSellerService adminSellerService;

  /** 전체 판매자 목록 조회. GET /api/admin/sellers */
  @GetMapping
  @Operation(summary = "전체 판매자 목록 조회", description = "관리자가 전체 판매자 목록을 조회합니다.")
  public ResponseEntity<List<SellerResponse>> getAllSellers() {
    return ResponseEntity.ok(adminSellerService.getAllSellers());
  }

  /** 승인 대기중(PENDING)인 판매자 목록 조회. GET /api/admin/sellers/pending */
  @GetMapping("/pending")
  @Operation(summary = "승인 대기 판매자 조회", description = "관리자가 승인 대기 중인 판매자 목록을 조회합니다.")
  public ResponseEntity<List<SellerResponse>> getPendingSellers() {
    return ResponseEntity.ok(adminSellerService.getPendingSellers());
  }

  /** 판매자 승인. PATCH /api/admin/sellers/{sellerId}/approve */
  @PatchMapping("/{sellerId}/approve")
  @Operation(summary = "판매자 승인", description = "관리자가 판매자 신청을 승인합니다.")
  public ResponseEntity<SellerResponse> approveSeller(@PathVariable Long sellerId) {
    return ResponseEntity.ok(adminSellerService.approveSeller(sellerId));
  }

  /** 판매자 정지. PATCH /api/admin/sellers/{sellerId}/suspend */
  @PatchMapping("/{sellerId}/suspend")
  @Operation(summary = "판매자 정지", description = "관리자가 판매자 계정을 정지 처리합니다.")
  public ResponseEntity<SellerResponse> suspendSeller(@PathVariable Long sellerId) {
    return ResponseEntity.ok(adminSellerService.suspendSeller(sellerId));
  }
}
