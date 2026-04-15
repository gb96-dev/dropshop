package com.example.dropshop.domain.wishlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dropshop.domain.wishlist.dto.request.WishlistRequest;
import com.example.dropshop.domain.wishlist.dto.response.WishlistResponse;
import com.example.dropshop.domain.wishlist.service.WishlistService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WishlistService wishlistService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("찜 생성 API - 성공")
  void createWishlist() throws Exception {

    // given
    WishlistResponse response =
        WishlistResponse.build(1L, LocalDateTime.now());

    given(wishlistService.create(any()))
        .willReturn(response);

    String json = """
            {
                "dropId": 1
            }
        """;

    // when & then
    mockMvc.perform(post("/api/wishlists")
            .contentType("application/json")
            .content(json))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(201))
        .andExpect(jsonPath("$.data.dropId").value(1));

    verify(wishlistService, times(1)).create(any());

    ArgumentCaptor<WishlistRequest> captor =
        ArgumentCaptor.forClass(WishlistRequest.class);

    verify(wishlistService).create(captor.capture());

    WishlistRequest captured = captor.getValue();
    assert captured.getDropId().equals(1L);
  }

  @Test
  @DisplayName("찜 취소 API - 성공")
  void cancelWishlist() throws Exception {

    // given
    String json = """
            {
                "dropId": 1
            }
        """;

    willDoNothing().given(wishlistService).cancel(any());

    // when & then
    mockMvc.perform(delete("/api/wishlists")
            .contentType("application/json")
            .content(json))
        .andExpect(status().isNoContent())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(204));

    verify(wishlistService, times(1)).cancel(any());

    ArgumentCaptor<WishlistRequest> captor =
        ArgumentCaptor.forClass(WishlistRequest.class);

    verify(wishlistService).cancel(captor.capture());

    assert captor.getValue().getDropId().equals(1L);
  }

  @Test
  @DisplayName("최근 찜 목록 조회 API - 성공")
  void getRecentWishlist() throws Exception {

    // given
    List<WishlistResponse> list = List.of(
        WishlistResponse.build(1L, LocalDateTime.now()),
        WishlistResponse.build(2L, LocalDateTime.now())
    );

    given(wishlistService.getRecent(5))
        .willReturn(list);

    // when & then
    mockMvc.perform(get("/api/wishlists")
            .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.length()").value(2));

    verify(wishlistService, times(1)).getRecent(5);
  }
}