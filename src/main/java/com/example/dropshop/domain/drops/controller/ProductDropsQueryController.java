package com.example.dropshop.domain.drops.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.drops.dto.response.DropListItemResponse;
import com.example.dropshop.domain.drops.service.DropsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품별 드롭 이력 조회 API 컨트롤러. 기술 설명. - 드롭 조회 책임을 공개 조회 컨트롤러와 분리해 명확한 라우팅 유지 - Page 응답을 공통
 * ApiResponse.PageResponse 형식으로 래핑
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "Product Drop Query", description = "상품별 드랍 조회 API")
public class ProductDropsQueryController {

  private final DropsQueryService dropsQueryService;

  /** 특정 상품의 드롭 이력을 조회한다. */
  @GetMapping("/{productId}/drops")
  @Operation(summary = "상품별 드랍 이력 조회", description = "특정 상품에 연결된 드랍 이력을 페이징으로 조회합니다.")
  public ResponseEntity<ApiResponse<ApiResponse.PageResponse<DropListItemResponse>>>
      getDropsByProduct(
          @PathVariable Long productId,
          @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0")
              int page,
          @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20")
              int size) {
    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<DropListItemResponse> response = dropsQueryService.findDropsByProduct(productId, pageable);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
