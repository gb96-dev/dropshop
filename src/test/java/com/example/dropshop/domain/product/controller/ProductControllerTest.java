package com.example.dropshop.domain.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.product.dto.request.ProductCreateRequest;
import com.example.dropshop.domain.product.dto.response.ProductCreateResponse;
import com.example.dropshop.domain.product.service.ProductFacadeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
@WithMockUser(roles = "SELLER")
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @MockitoBean
  private ProductFacadeService productFacadeService;

  @MockitoBean
  private SellerAuthResolver sellerAuthResolver;

  @Test
  @DisplayName("상품 등록 성공")
  void createProduct_success() throws Exception {
    ProductCreateRequest request = new ProductCreateRequest();
    ReflectionTestUtils.setField(request, "name", "한정판 스니커즈");
    ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(250000));
    ReflectionTestUtils.setField(request, "discountRate", 10);
    ReflectionTestUtils.setField(request, "stock", 100);
    ReflectionTestUtils.setField(request, "category", "SHOES");
    ReflectionTestUtils.setField(request, "description", "<p>상품 설명 HTML</p>");
    ReflectionTestUtils.setField(request, "specification", "사이즈: 255 / 소재: 가죽");
    ReflectionTestUtils.setField(request, "images", List.of(createImageRequest()));

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
      .createdAt(LocalDateTime.of(2026, 4, 10, 10, 0))
      .build();

    given(sellerAuthResolver.resolve(any()))
      .willReturn(new SellerAuthContext(1L, true, true));
    given(productFacadeService.createSellerProduct(
      eq(1L),
      eq(true),
      eq(true),
      any(ProductCreateRequest.class)
    )).willReturn(response);

    mockMvc.perform(post("/api/sellers/products")
        .with(csrf())
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.code").value(201))
      .andExpect(jsonPath("$.data.productId").value(42L))
      .andExpect(jsonPath("$.data.status").value("HIDDEN"));
  }

  private ProductCreateRequest.ImageRequest createImageRequest() {
    ProductCreateRequest.ImageRequest imageRequest = new ProductCreateRequest.ImageRequest();
    ReflectionTestUtils.setField(imageRequest, "imageUrl", "https://cdn.example.com/img1.jpg");
    ReflectionTestUtils.setField(imageRequest, "sortOrder", 1);
    ReflectionTestUtils.setField(imageRequest, "isThumbnail", true);
    return imageRequest;
  }
}
