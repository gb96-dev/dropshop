package com.example.dropshop.domain.drops.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dropshop.common.security.SellerAuthContext;
import com.example.dropshop.common.security.SellerAuthResolver;
import com.example.dropshop.domain.drops.dto.request.DropCreateRequest;
import com.example.dropshop.domain.drops.dto.response.DropResponse;
import com.example.dropshop.domain.drops.service.DropsFacadeService;
import com.example.dropshop.domain.drops.service.DropsQueryService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DropsController.class)
@WithMockUser(roles = "SELLER")
class DropsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @MockitoBean
  private DropsFacadeService dropsFacadeService;

  @MockitoBean
  private DropsQueryService dropsQueryService;

  @MockitoBean
  private SellerAuthResolver sellerAuthResolver;

  @Test
  @DisplayName("드랍 생성 성공")
  void createDrop_success() throws Exception {
    DropCreateRequest request = new DropCreateRequest();
    ReflectionTestUtils.setField(request, "productId", 1L);
    ReflectionTestUtils.setField(request, "startAt", LocalDateTime.of(2026, 4, 25, 12, 0));
    ReflectionTestUtils.setField(request, "endAt", LocalDateTime.of(2026, 4, 26, 12, 0));
    ReflectionTestUtils.setField(request, "totalStock", 30L);
    ReflectionTestUtils.setField(request, "purchaseLimit", 1L);
    ReflectionTestUtils.setField(request, "useQueue", true);

    DropResponse response = DropResponse.builder()
        .dropId(10L)
        .productId(1L)
        .status("SCHEDULED")
        .startAt(LocalDateTime.of(2026, 4, 25, 12, 0))
        .endAt(LocalDateTime.of(2026, 4, 26, 12, 0))
        .totalStock(30L)
        .remainStock(30L)
        .purchaseLimit(1L)
        .useQueue(true)
        .createdAt(LocalDateTime.of(2026, 4, 20, 10, 0))
        .modifiedAt(LocalDateTime.of(2026, 4, 20, 10, 0))
        .build();

    given(sellerAuthResolver.resolve(any()))
        .willReturn(new SellerAuthContext(1L, true, true));
    given(dropsFacadeService.createSellerDrop(eq(1L), eq(true), eq(true), any(DropCreateRequest.class)))
        .willReturn(response);

    mockMvc.perform(post("/api/sellers/drops")
            .with(csrf())
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.data.dropId").value(10L))
        .andExpect(jsonPath("$.data.productId").value(1L))
        .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
  }

  @Test
  @DisplayName("드랍 강제 종료 성공")
  void stopDrop_success() throws Exception {
    DropResponse response = DropResponse.builder()
        .dropId(10L)
        .productId(1L)
        .status("FINISHED")
        .startAt(LocalDateTime.of(2026, 4, 25, 12, 0))
        .endAt(LocalDateTime.of(2026, 4, 26, 12, 0))
        .totalStock(30L)
        .remainStock(12L)
        .purchaseLimit(1L)
        .useQueue(true)
        .createdAt(LocalDateTime.of(2026, 4, 20, 10, 0))
        .modifiedAt(LocalDateTime.of(2026, 4, 20, 10, 30))
        .build();

    given(sellerAuthResolver.resolve(any()))
        .willReturn(new SellerAuthContext(1L, true, true));
    given(dropsFacadeService.stopSellerDrop(10L, 1L, true, true)).willReturn(response);

    mockMvc.perform(patch("/api/sellers/drops/{id}/stop", 10L)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.dropId").value(10L))
        .andExpect(jsonPath("$.data.status").value("FINISHED"));
  }

  @Test
  @DisplayName("SELLER 권한이 없으면 드랍 생성이 거부된다")
  @WithAnonymousUser
  void createDrop_unauthorized_forbidden() throws Exception {
    mockMvc.perform(post("/api/sellers/drops")
            .with(csrf())
            .contentType(APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isForbidden());
  }
}


