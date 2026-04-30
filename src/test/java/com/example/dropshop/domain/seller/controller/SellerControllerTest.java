package com.example.dropshop.domain.seller.controller;

import com.example.dropshop.domain.seller.dto.request.SellerApplyRequest;
import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import com.example.dropshop.domain.seller.entity.Seller;
import com.example.dropshop.domain.seller.enums.SellerStatus;
import com.example.dropshop.domain.seller.service.SellerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SellerControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private SellerController sellerController;

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver mockAuthResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(String.class)
                        && parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return "test@example.com";
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(sellerController)
                .setCustomArgumentResolvers(mockAuthResolver)
                .build();
    }

    @Test
    @DisplayName("판매자 신청 API 테스트 - 성공 및 데이터 바인딩 검증")
    void applySeller_Success() throws Exception {
        // given
        Seller mockSeller = Mockito.mock(Seller.class);
        given(mockSeller.getBrandName()).willReturn("드랍숍");
        given(mockSeller.getStatus()).willReturn(SellerStatus.PENDING);

        SellerResponse response = new SellerResponse(mockSeller);
        given(sellerService.applySeller(anyString(), any(SellerApplyRequest.class)))
                .willReturn(response);

        // CodeRabbit 지적 반영: brandLogoUrl -> brandLogo 로 수정
        String jsonContent = "{" +
                "\"companyName\":\"테스트법인\"," +
                "\"representativeName\":\"홍길동\"," +
                "\"phoneNumber\":\"01012345678\"," +
                "\"businessNo\":\"1234567890\"," +
                "\"brandName\":\"드랍숍\"," +
                "\"brandLogo\":\"logo.png\"," +
                "\"accountInfo\":\"신한 110-123\"" +
                "}";

        // when
        mockMvc.perform(post("/api/sellers/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandName").value("드랍숍"));

        // then: ArgumentCaptor를 사용하여 실제 전달된 데이터 검증
        ArgumentCaptor<SellerApplyRequest> requestCaptor = ArgumentCaptor.forClass(SellerApplyRequest.class);
        verify(sellerService).applySeller(anyString(), requestCaptor.capture());

        SellerApplyRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getBusinessNo()).isEqualTo("1234567890");
        assertThat(capturedRequest.getBrandName()).isEqualTo("드랍숍");
        assertThat(capturedRequest.getBrandLogo()).isEqualTo("logo.png"); // 바인딩 확인
        assertThat(capturedRequest.getAccountInfo()).isEqualTo("신한 110-123");
    }

    @Test
    @DisplayName("판매자 신청 실패 - 비어있는 브랜드명 (400)")
    void applySeller_Fail_EmptyBrandName() throws Exception {
        String invalidJson = "{" +
                "\"businessNo\":\"1234567890\"," +
                "\"brandName\":\"\"," +
                "\"brandLogo\":\"logo.png\"," +
                "\"accountInfo\":\"신한 110-123\"" +
                "}";

        mockMvc.perform(post("/api/sellers/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(sellerService, never()).applySeller(anyString(), any(SellerApplyRequest.class));
    }

    @Test
    @DisplayName("판매자 신청 실패 - 잘못된 사업자 번호 형식 (400)")
    void applySeller_Fail_InvalidBusinessNo() throws Exception {
        String invalidJson = "{" +
                "\"businessNo\":\"ABC-12-345\"," +
                "\"brandName\":\"드랍숍\"," +
                "\"brandLogo\":\"logo.png\"," +
                "\"accountInfo\":\"신한 110-123\"" +
                "}";

        mockMvc.perform(post("/api/sellers/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(sellerService, never()).applySeller(anyString(), any(SellerApplyRequest.class));
    }

    @Test
    @DisplayName("내 판매자 상태 조회 API 테스트 - 성공")
    void getMySellerStatus_Success() throws Exception {
        Seller mockSeller = Mockito.mock(Seller.class);
        given(mockSeller.getBrandName()).willReturn("드랍숍");
        given(mockSeller.getStatus()).willReturn(SellerStatus.APPROVED);

        SellerResponse response = new SellerResponse(mockSeller);
        given(sellerService.getMySellerStatus(anyString())).willReturn(response);

        mockMvc.perform(get("/api/sellers/me/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}