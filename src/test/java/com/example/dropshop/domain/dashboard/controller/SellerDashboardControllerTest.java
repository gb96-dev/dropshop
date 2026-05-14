package com.example.dropshop.domain.dashboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.auth.service.TokenBlacklistService;
import com.example.dropshop.domain.dashboard.dto.response.SellerDashboardOrderItemResponse;
import com.example.dropshop.domain.dashboard.service.SellerDashboardQueryService;
import com.example.dropshop.domain.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SellerDashboardController.class)
class SellerDashboardControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @MockitoBean private SellerDashboardQueryService sellerDashboardQueryService;

  @MockitoBean private SellerAuthResolver sellerAuthResolver;

  @MockitoBean private TokenBlacklistService tokenBlacklistService;

  @Test
  @DisplayName("대시보드 주문 목록 조회 성공")
  void getOrders_success() throws Exception {
    SellerAuthContext sellerAuth = new SellerAuthContext(1L, true, true);
    SellerDashboardOrderItemResponse item =
        new SellerDashboardOrderItemResponse(
            1L,
            "ORDER-TEST",
            10L,
            100L,
            "테스트 상품",
            "https://dummy-image",
            1,
            new BigDecimal("79000"),
            OrderStatus.PAID,
            LocalDateTime.of(2026, 5, 10, 10, 0));
    Page<SellerDashboardOrderItemResponse> page =
        new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);

    given(sellerAuthResolver.resolve(any())).willReturn(sellerAuth);
    given(sellerDashboardQueryService.getOrderItems(eq(1L), eq(null), eq(null), eq(null), any()))
        .willReturn(page);

    mockMvc
        .perform(
            get("/api/sellers/me/dashboard/orders")
                .with(authentication(testAuthentication()))
                .param("page", "0")
                .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content.length()").value(1))
        .andExpect(jsonPath("$.data.content[0].orderId").value(1L))
        .andExpect(jsonPath("$.data.pageInfo.page").value(0))
        .andExpect(jsonPath("$.data.pageInfo.size").value(20));
  }

  @Test
  @DisplayName("대시보드 주문 목록 조회 실패 - size 상한 초과 시 400")
  void getOrders_invalidSize_returnsBadRequest() throws Exception {
    mockMvc
        .perform(
            get("/api/sellers/me/dashboard/orders")
                .with(authentication(testAuthentication()))
                .param("size", "100000"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.data.message").exists());
  }

  private static Authentication testAuthentication() {
    return new UsernamePasswordAuthenticationToken(
        "seller@test.com", null, List.of(new SimpleGrantedAuthority("ROLE_SELLER")));
  }
}
