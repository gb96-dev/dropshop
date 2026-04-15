package com.example.dropshop.domain.wishlist.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.wishlist.dto.request.WishlistRequest;
import com.example.dropshop.domain.wishlist.dto.response.WishlistResponse;
import com.example.dropshop.domain.wishlist.service.WishlistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 찜 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlists")
public class WishlistController {

  private final WishlistService wishlistService;

  /**
   * 찜 생성.
   * @param request 요청.
   * @return 리턴.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<WishlistResponse>> create(
      @RequestBody WishlistRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(wishlistService.create(request)));
  }

  /**
   * 찜 취소.
   * @param request 요청.
   * @return 리턴.
   */
  @DeleteMapping
  public ResponseEntity<ApiResponse<WishlistResponse>> cancel(
      @RequestBody WishlistRequest request
  ) {
    wishlistService.cancel(request);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.noContent());
  }

  /**
   * 최근 찜 목록 가져오기.
   * @param size 조회 사이즈.
   * @return 리턴.
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<WishlistResponse>>> getRecent(
      @RequestParam(defaultValue = "10") int size
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(wishlistService.getRecent(size)));
  }
}