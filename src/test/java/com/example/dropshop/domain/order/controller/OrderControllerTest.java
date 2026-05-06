package com.example.dropshop.domain.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dropshop.domain.order.dto.request.OrderCreateRequest;
import com.example.dropshop.domain.order.dto.response.OrderCreateResponse;
import com.example.dropshop.domain.order.dto.response.OrderDetailResponse;
import com.example.dropshop.domain.order.dto.response.OrderListItemResponse;
import com.example.dropshop.domain.order.entity.Order;
import com.example.dropshop.domain.order.entity.OrderItem;
import com.example.dropshop.domain.auth.service.TokenBlacklistService;
import com.example.dropshop.domain.order.facade.OrderFacadeService;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @MockitoBean
  private OrderFacadeService orderFacadeService;

  @MockitoBean
  private TokenBlacklistService tokenBlacklistService;

  @Test
  @DisplayName("주문 생성 성공")
  void createOrder_success() throws Exception {
    // given
    OrderCreateRequest request = new OrderCreateRequest();
    ReflectionTestUtils.setField(request, "dropId", 1L);
    ReflectionTestUtils.setField(request, "productId", 100L);
    ReflectionTestUtils.setField(request, "queueToken", "queue-token");

    Order order = createOrderEntity(1L, 1L, 100L);
    OrderCreateResponse response = OrderCreateResponse.from(order);

    given(orderFacadeService.createOrder(any(), any(OrderCreateRequest.class)))
        .willReturn(response);

    // when & then
    mockMvc.perform(post("/api/orders")
            .with(authentication(testAuthentication()))
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orderId").value(1L))
        .andExpect(jsonPath("$.data.orderNumber").value(order.getOrderNumber()))
        .andExpect(jsonPath("$.data.status").value("PENDING"))
        .andExpect(jsonPath("$.data.totalAmount").value(79000))
        .andExpect(jsonPath("$.data.orderItems.length()").value(1))
        .andExpect(jsonPath("$.data.orderItems[0].productId").value(100L));
  }

  @Test
  @DisplayName("주문 단건 조회 성공")
  void findOrderById_success() throws Exception {
    // given
    Order order = createOrderEntity(1L, 10L, 100L);
    OrderDetailResponse response = OrderDetailResponse.from(order);

    given(orderFacadeService.findOrderById(eq(1L), any())).willReturn(response);

    // when & then
    mockMvc.perform(get("/api/orders/{orderId}", 1L)
            .with(authentication(testAuthentication())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orderId").value(1L))
        .andExpect(jsonPath("$.data.orderNumber").value(order.getOrderNumber()))
        .andExpect(jsonPath("$.data.status").value("PENDING"))
        .andExpect(jsonPath("$.data.totalAmount").value(79000))
        .andExpect(jsonPath("$.data.orderItems.length()").value(1))
        .andExpect(jsonPath("$.data.orderItems[0].productId").value(100L));
  }

  @Test
  @DisplayName("주문 목록 조회 성공")
  void findOrders_success() throws Exception {
    // given
    Order order = createOrderEntity(1L, 10L, 100L);
    OrderListItemResponse itemResponse = OrderListItemResponse.from(order);

    Page<OrderListItemResponse> page = new PageImpl<>(
        List.of(itemResponse),
        PageRequest.of(0, 20),
        1
    );

    given(orderFacadeService.findOrdersByUserId(any(), any()))
        .willReturn(page);

    // when & then
    mockMvc.perform(get("/api/orders")
            .with(authentication(testAuthentication()))
            .param("page", "0")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content.length()").value(1))
        .andExpect(jsonPath("$.data.content[0].orderId").value(1L))
        .andExpect(jsonPath("$.data.content[0].dropId").value(10L))
        .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
        .andExpect(jsonPath("$.data.pageInfo.page").value(0))
        .andExpect(jsonPath("$.data.pageInfo.size").value(20))
        .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1))
        .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1))
        .andExpect(jsonPath("$.data.pageInfo.isFirst").value(true))
        .andExpect(jsonPath("$.data.pageInfo.isLast").value(true));
  }

  @Test
  @DisplayName("주문 수동 취소 성공")
  void cancelOrder_success() throws Exception {
    // given
    Order order = createOrderEntity(1L, 10L, 100L);
    order.cancel();
    OrderDetailResponse response = OrderDetailResponse.from(order);

    given(orderFacadeService.cancelOrder(eq(1L), any())).willReturn(response);

    // when & then
    mockMvc.perform(patch("/api/orders/{orderId}/cancel", 1L)
            .with(authentication(testAuthentication())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orderId").value(1L))
        .andExpect(jsonPath("$.data.status").value("CANCELLED"));
  }

  private Order createOrderEntity(Long userId, Long dropId, Long productId) {
    Order order = Order.create(userId, dropId);
    ReflectionTestUtils.setField(order, "id", 1L);

    OrderItem orderItem = OrderItem.create(
        order,
        productId,
        new BigDecimal("100000"),
        new BigDecimal("79000"),
        new BigDecimal("21000"),
        "https://dummy-image"
    );
    order.addOrderItem(orderItem);

    ReflectionTestUtils.setField(
        order,
        "holdExpiredAt",
        LocalDateTime.of(2026, 4, 9, 12, 5, 0)
    );

    return order;
  }

  private static Authentication testAuthentication() {
    return new UsernamePasswordAuthenticationToken(
        "test@test.com",
        null,
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
  }
}
