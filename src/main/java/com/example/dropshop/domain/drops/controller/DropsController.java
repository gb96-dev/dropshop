package com.example.dropshop.domain.drops.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.drops.dto.request.DropCreateRequest;
import com.example.dropshop.domain.drops.dto.request.DropUpdateRequest;
import com.example.dropshop.domain.drops.dto.response.DropListItemResponse;
import com.example.dropshop.domain.drops.dto.response.DropResponse;
import com.example.dropshop.domain.drops.service.DropsFacadeService;
import com.example.dropshop.domain.drops.service.DropsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 판매자 드랍 API 컨트롤러. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/drops")
@Tag(name = "Seller Drop", description = "판매자 드랍 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class DropsController {

  private final DropsFacadeService dropsFacadeService;
  private final DropsQueryService dropsQueryService;
  private final SellerAuthResolver sellerAuthResolver;

  /** 판매자가 새로운 드랍을 생성한다. */
  @PostMapping
  @Operation(summary = "드랍 생성", description = "판매자가 새로운 드랍을 생성합니다.")
  public ResponseEntity<ApiResponse<DropResponse>> createDrop(
      @AuthenticationPrincipal String email, @Valid @RequestBody DropCreateRequest request) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    DropResponse response =
        dropsFacadeService.createSellerDrop(
            sellerAuth.sellerId(),
            sellerAuth.sellerApproved(),
            sellerAuth.sellerVerified(),
            request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  /** 판매자가 본인 드랍을 수정한다. */
  @PatchMapping("/{id}")
  @Operation(summary = "드랍 수정", description = "판매자가 본인 드랍 정보를 수정합니다.")
  public ResponseEntity<ApiResponse<DropResponse>> updateDrop(
      @PathVariable Long id,
      @AuthenticationPrincipal String email,
      @Valid @RequestBody DropUpdateRequest request) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    DropResponse response =
        dropsFacadeService.updateSellerDrop(
            id,
            sellerAuth.sellerId(),
            sellerAuth.sellerApproved(),
            sellerAuth.sellerVerified(),
            request);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /** 판매자가 본인 드랍을 삭제한다. */
  @DeleteMapping("/{id}")
  @Operation(summary = "드랍 삭제", description = "판매자가 본인 드랍을 삭제합니다.")
  public ResponseEntity<ApiResponse<Void>> deleteDrop(
      @PathVariable Long id, @AuthenticationPrincipal String email) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    dropsFacadeService.deleteSellerDrop(
        id, sellerAuth.sellerId(), sellerAuth.sellerApproved(), sellerAuth.sellerVerified());
    return ResponseEntity.ok(ApiResponse.ok());
  }

  /** 판매자가 드랍을 강제 종료한다. */
  @PatchMapping("/{id}/stop")
  @Operation(summary = "드랍 강제 종료", description = "판매자가 진행 중인 드랍을 강제로 종료합니다.")
  public ResponseEntity<ApiResponse<DropResponse>> stopDrop(
      @PathVariable Long id, @AuthenticationPrincipal String email) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    DropResponse response =
        dropsFacadeService.stopSellerDrop(
            id, sellerAuth.sellerId(), sellerAuth.sellerApproved(), sellerAuth.sellerVerified());
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  /** 판매자 본인 드롭 목록 조회. */
  @GetMapping("/mine")
  @Operation(summary = "내 드랍 목록 조회", description = "로그인한 판매자의 드랍 목록을 페이징으로 조회합니다.")
  public ResponseEntity<ApiResponse<ApiResponse.PageResponse<DropListItemResponse>>> getMyDrops(
      @AuthenticationPrincipal String email,
      @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20")
          int size) {
    SellerAuthContext sellerAuth = sellerAuthResolver.resolve(email);
    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<DropListItemResponse> response =
        dropsQueryService.findSellerDrops(sellerAuth.sellerId(), pageable);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
