package com.example.dropshop.domain.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.auth.service.TokenBlacklistService;
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.product.dto.request.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.response.ProductCreateResponse;
import com.example.dropshop.domain.product.service.ProductFacadeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @MockitoBean
  private ProductFacadeService productFacadeService;

  @MockitoBean
  private SellerAuthResolver sellerAuthResolver;

  @MockitoBean
  private TokenBlacklistService tokenBlacklistService;

  @Test
  @DisplayName("상품 등록 성공 - @AuthenticationPrincipal email 사용")
  void createProduct_success() throws Exception {
    SellerAuthContext sellerAuth = new SellerAuthContext(1L, true, true);
    ProductCreateResponse response = ProductCreateResponse.builder()
        .productId(42L)
        .name("한정판 스니커즈")
        .price(BigDecimal.valueOf(250000))
        .discountRate(10)
        .discountAmount(BigDecimal.valueOf(25000))
        .salePrice(BigDecimal.valueOf(225000))
        .stock(100)
        .category("SHOES")
        .status("HIDDEN")
        .thumbnailUrl("https://cdn.example.com/img1.jpg")
        .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0, 0))
        .build();

    given(sellerAuthResolver.resolve(any())).willReturn(sellerAuth);
    given(productFacadeService.createSellerProduct(
        eq(1L),
        eq(true),
        eq(true),
        any(ProductCreateRequest.class)
    )).willReturn(response);

    String request = """
        {
          "name": "한정판 스니커즈",
          "price": 250000,
          "discountRate": 10,
          "stock": 100,
          "category": "SHOES",
          "description": "<p>상품 설명 HTML</p>",
          "specification": "사이즈: 255 / 소재: 가죽",
          "images": [
            {
              "imageUrl": "https://cdn.example.com/img1.jpg",
              "sortOrder": 1,
              "isThumbnail": true
            }
          ]
        }
        """;

    mockMvc.perform(post("/api/sellers/products")
            .with(csrf())
            .contentType(APPLICATION_JSON)
            .content(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.data.productId").value(42L))
        .andExpect(jsonPath("$.data.name").value("한정판 스니커즈"))
        .andExpect(jsonPath("$.data.price").value(250000))
        .andExpect(jsonPath("$.data.discountRate").value(10))
        .andExpect(jsonPath("$.data.salePrice").value(225000))
        .andExpect(jsonPath("$.data.stock").value(100))
        .andExpect(jsonPath("$.data.category").value("SHOES"))
        .andExpect(jsonPath("$.data.status").value("HIDDEN"))
        .andExpect(jsonPath("$.data.thumbnailUrl").value("https://cdn.example.com/img1.jpg"));
  }

  @Test
  @DisplayName("상품 등록 실패 - 유효성 검증 실패 시 400")
  void createProduct_validationFail() throws Exception {
    String invalidRequest = """
        {
          "name": "",
          "price": 250000,
          "discountRate": 10,
          "stock": 100,
          "category": "SHOES",
          "description": "desc",
          "specification": "spec",
          "images": []
        }
        """;

    mockMvc.perform(post("/api/sellers/products")
            .with(csrf())
            .contentType(APPLICATION_JSON)
            .content(invalidRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.data.message").exists());
  }

  @Test
  @DisplayName("상품 등록 실패 - 미인증 요청")
  void createProduct_unauthorized() throws Exception {
    // 인증 컨텍스트를 해석하지 못한 경우(미인증/권한 없음)를 resolver에서 표현한다.
    given(sellerAuthResolver.resolve(any()))
        .willThrow(new ServiceException(ErrorCode.SELLER_ROLE_REQUIRED));

    String request = """
        {
          "name": "한정판 스니커즈",
          "price": 250000,
          "discountRate": 10,
          "stock": 100,
          "category": "SHOES",
          "description": "desc",
          "specification": "spec",
          "images": [{"imageUrl":"https://cdn.example.com/img1.jpg","sortOrder":1,"isThumbnail":true}]
        }
        """;

    mockMvc.perform(post("/api/sellers/products")
            .with(csrf())
            .contentType(APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value(403));

    verifyNoInteractions(productFacadeService);
  }

  @Test
  @DisplayName("상품 등록 실패 - 서비스 예외 응답 구조")
  void createProduct_serviceException() throws Exception {
    SellerAuthContext sellerAuth = new SellerAuthContext(1L, true, true);
    given(sellerAuthResolver.resolve(any())).willReturn(sellerAuth);
    given(productFacadeService.createSellerProduct(
        eq(1L),
        eq(true),
        eq(true),
        any(ProductCreateRequest.class)
    )).willThrow(new ServiceException(ErrorCode.SELLER_NOT_APPROVED));

    String request = """
        {
          "name": "한정판 스니커즈",
          "price": 250000,
          "discountRate": 10,
          "stock": 100,
          "category": "SHOES",
          "description": "desc",
          "specification": "spec",
          "images": [{"imageUrl":"https://cdn.example.com/img1.jpg","sortOrder":1,"isThumbnail":true}]
        }
        """;

    mockMvc.perform(post("/api/sellers/products")
            .with(csrf())
            .contentType(APPLICATION_JSON)
            .content(request))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value(403))
        .andExpect(jsonPath("$.message").value(ErrorCode.SELLER_NOT_APPROVED.getMessage()));

    verify(productFacadeService).createSellerProduct(eq(1L), eq(true), eq(true), any());
  }
}



