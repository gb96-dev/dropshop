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
import com.example.dropshop.common.exception.ServiceException;
import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.auth.service.TokenBlacklistService;
import com.example.dropshop.domain.product.dto.request.PresignedUrlIssueRequest;
import com.example.dropshop.domain.product.dto.response.PresignedUrlIssueResponse;
import com.example.dropshop.domain.product.service.ProductImageUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SellerImageController.class)
class SellerImageControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @MockitoBean private ProductImageUploadService productImageUploadService;

  @MockitoBean private SellerAuthResolver sellerAuthResolver;

  @MockitoBean private TokenBlacklistService tokenBlacklistService;

  @Test
  @DisplayName("Presigned URL 발급 성공 - @AuthenticationPrincipal email 사용")
  void issuePresignedUrl_success() throws Exception {
    SellerAuthContext sellerAuth = new SellerAuthContext(1L, true, true);
    PresignedUrlIssueResponse response =
        PresignedUrlIssueResponse.builder()
            .presignedUrl("https://s3.example.com/presigned")
            .imageUrl("https://cdn.example.com/image.png")
            .build();

    given(sellerAuthResolver.resolve(any())).willReturn(sellerAuth);
    given(productImageUploadService.issuePresignedUrl(eq(1L), any(PresignedUrlIssueRequest.class)))
        .willReturn(response);

    String request =
        """
        {
          "fileType": "png"
        }
        """;

    mockMvc
        .perform(
            post("/api/sellers/images/presigned-url")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.data.presignedUrl").value("https://s3.example.com/presigned"))
        .andExpect(jsonPath("$.data.imageUrl").value("https://cdn.example.com/image.png"));
  }

  @Test
  @DisplayName("Presigned URL 발급 실패 - 유효성 검증 실패 시 400")
  void issuePresignedUrl_validationFail() throws Exception {
    mockMvc
        .perform(
            post("/api/sellers/images/presigned-url")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.data.message").exists());
  }

  @Test
  @DisplayName("Presigned URL 발급 실패 - 미인증 요청")
  void issuePresignedUrl_unauthorized() throws Exception {
    // 인증 컨텍스트를 해석하지 못한 경우(미인증/권한 없음)를 resolver에서 표현한다.
    given(sellerAuthResolver.resolve(any()))
        .willThrow(new ServiceException(ErrorCode.SELLER_ROLE_REQUIRED));

    String request =
        """
        {
          "fileType": "png"
        }
        """;

    mockMvc
        .perform(
            post("/api/sellers/images/presigned-url")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(request))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value(403));

    verifyNoInteractions(productImageUploadService);
  }

  @Test
  @DisplayName("Presigned URL 발급 실패 - 서비스 예외 응답 구조")
  void issuePresignedUrl_serviceException() throws Exception {
    SellerAuthContext sellerAuth = new SellerAuthContext(1L, true, true);
    given(sellerAuthResolver.resolve(any())).willReturn(sellerAuth);
    given(productImageUploadService.issuePresignedUrl(eq(1L), any(PresignedUrlIssueRequest.class)))
        .willThrow(new ServiceException(ErrorCode.PRESIGNED_URL_GENERATION_FAILED));

    String request =
        """
        {
          "fileType": "png"
        }
        """;

    mockMvc
        .perform(
            post("/api/sellers/images/presigned-url")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(request))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.errorCode").value(500))
        .andExpect(
            jsonPath("$.message").value(ErrorCode.PRESIGNED_URL_GENERATION_FAILED.getMessage()));

    verify(productImageUploadService)
        .issuePresignedUrl(eq(1L), any(PresignedUrlIssueRequest.class));
  }
}
