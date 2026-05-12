package com.example.dropshop.domain.wishlist.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.wishlist.dto.request.WishlistRequest;
import com.example.dropshop.domain.wishlist.dto.response.WishlistResponse;
import com.example.dropshop.domain.wishlist.facade.WishlistsFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 찜 컨트롤러. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlists")
@Tag(name = "Wishlist", description = "찜 API")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

  private final WishlistsFacadeService wishlistsFacadeService;

  /**
   * 찜 생성.
   *
   * @param request 요청.
   * @return 리턴.
   */
  @PostMapping
  @Operation(summary = "찜 등록", description = "상품을 내 찜 목록에 추가합니다.")
  public ResponseEntity<ApiResponse<WishlistResponse>> create(
      @AuthenticationPrincipal String userEmail, @RequestBody WishlistRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(wishlistsFacadeService.create(userEmail, request)));
  }

  /**
   * 찜 취소.
   *
   * @param request 요청.
   * @return 리턴.
   */
  @DeleteMapping
  @Operation(summary = "찜 해제", description = "상품을 내 찜 목록에서 제거합니다.")
  public ResponseEntity<ApiResponse<WishlistResponse>> cancel(
      @AuthenticationPrincipal String userEmail, @RequestBody WishlistRequest request) {
    wishlistsFacadeService.cancel(userEmail, request);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.noContent());
  }

  /**
   * 최근 찜 목록 가져오기.
   *
   * @param size 조회 사이즈.
   * @return 리턴.
   */
  @GetMapping
  @Operation(summary = "최근 찜 목록 조회", description = "로그인한 사용자의 최근 찜 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<List<WishlistResponse>>> getRecent(
      @AuthenticationPrincipal String userEmail, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.ok(wishlistsFacadeService.getRecent(userEmail, size)));
  }
}
