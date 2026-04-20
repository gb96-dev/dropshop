package com.example.dropshop.domain.drops.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.drops.dto.request.DropCreateRequest;
import com.example.dropshop.domain.drops.dto.request.DropUpdateRequest;
import com.example.dropshop.domain.drops.dto.response.DropResponse;
import com.example.dropshop.domain.drops.exception.DropsException;
import com.example.dropshop.domain.drops.service.DropsFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매자 드랍 API 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/drops")
public class DropsController {

  private final DropsFacadeService dropsFacadeService;

  /**
   * 판매자가 새로운 드랍을 생성한다.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<DropResponse>> createDrop(
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified,
      @Valid @RequestBody DropCreateRequest request
  ) {
    validateSellerRole(role);
    DropResponse response = dropsFacadeService.createSellerDrop(
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  /**
   * 판매자가 본인 드랍을 수정한다.
   */
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<DropResponse>> updateDrop(
      @PathVariable Long id,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified,
      @Valid @RequestBody DropUpdateRequest request
  ) {
    validateSellerRole(role);
    DropResponse response = dropsFacadeService.updateSellerDrop(
        id,
        sellerId,
        sellerApproved,
        sellerVerified,
        request
    );
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /**
   * 판매자가 본인 드랍을 삭제한다.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteDrop(
      @PathVariable Long id,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified
  ) {
    validateSellerRole(role);
    dropsFacadeService.deleteSellerDrop(id, sellerId, sellerApproved, sellerVerified);
    return ResponseEntity.ok(ApiResponse.ok());
  }

  /**
   * 판매자가 드랍을 강제 종료한다.
   */
  @PatchMapping("/{id}/stop")
  public ResponseEntity<ApiResponse<DropResponse>> stopDrop(
      @PathVariable Long id,
      @RequestHeader("X-SELLER-ID") Long sellerId,
      @RequestHeader(value = "X-ROLE", required = false) String role,
      @RequestHeader(value = "X-SELLER-APPROVED", defaultValue = "false") boolean sellerApproved,
      @RequestHeader(value = "X-SELLER-VERIFIED", defaultValue = "false") boolean sellerVerified
  ) {
    validateSellerRole(role);
    DropResponse response = dropsFacadeService.stopSellerDrop(
        id,
        sellerId,
        sellerApproved,
        sellerVerified
    );
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  private void validateSellerRole(String role) {
    if (role != null && !"SELLER".equalsIgnoreCase(role)) {
      throw new DropsException(ErrorCode.SELLER_ROLE_REQUIRED);
    }
  }
}

