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
import com.example.dropshop.domain.product.dto.request.PresignedUrlIssueRequest;
import com.example.dropshop.domain.product.dto.response.PresignedUrlIssueResponse;
import com.example.dropshop.domain.product.service.ProductImageUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(SellerImageController.class)
class SellerImageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @MockitoBean
  private ProductImageUploadService productImageUploadService;

  @MockitoBean
  private SellerAuthResolver sellerAuthResolver;

  @Test
  @DisplayName("Presigned URL 발급 성공")
  void issuePresignedUrl_success() throws Exception {
    PresignedUrlIssueRequest request = new PresignedUrlIssueRequest();
    ReflectionTestUtils.setField(request, "fileType", "image/png");

    PresignedUrlIssueResponse response = PresignedUrlIssueResponse.builder()
        .presignedUrl("https://s3.example.com/presigned-url")
        .imageUrl("https://cdn.example.com/products/1/test.png")
        .build();

    given(sellerAuthResolver.resolve(any()))
        .willReturn(new SellerAuthContext(1L, true, true));
    given(productImageUploadService.issuePresignedUrl(eq(1L), any(PresignedUrlIssueRequest.class)))
        .willReturn(response);

    mockMvc.perform(post("/api/sellers/images/presigned-url")
            .with(csrf())
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.data.presignedUrl").value("https://s3.example.com/presigned-url"))
        .andExpect(jsonPath("$.data.imageUrl")
            .value("https://cdn.example.com/products/1/test.png"));
  }
}


