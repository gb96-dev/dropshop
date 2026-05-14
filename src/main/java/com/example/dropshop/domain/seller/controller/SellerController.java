package com.example.dropshop.domain.seller.controller;

import com.example.dropshop.domain.seller.dto.request.SellerApplyRequest;
import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import com.example.dropshop.domain.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Tag(name = "Seller", description = "판매자 신청 및 상태 조회 API")
public class SellerController {

  private final SellerService sellerService;

  @PostMapping("/apply")
  @Operation(summary = "판매자 신청", description = "로그인한 사용자가 판매자 전환을 신청합니다.")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<SellerResponse> applySeller(
      @AuthenticationPrincipal String email, @Valid @RequestBody SellerApplyRequest request) {
    return ResponseEntity.ok(sellerService.applySeller(email, request));
  }

  @GetMapping("/me/status")
  @Operation(summary = "내 판매자 상태 조회", description = "로그인한 사용자의 판매자 승인 상태를 조회합니다.")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<SellerResponse> getMyStatus(@AuthenticationPrincipal String email) {
    return ResponseEntity.ok(sellerService.getMySellerStatus(email));
  }
}
