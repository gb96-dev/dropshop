package com.example.dropshop.domain.seller.controller;

import com.example.dropshop.domain.seller.dto.request.SellerApplyRequest;
import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import com.example.dropshop.domain.seller.entity.Seller;
import com.example.dropshop.domain.seller.enums.SellerStatus;
import com.example.dropshop.domain.seller.service.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SellerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private SellerController sellerController;

    @BeforeEach
    void setUp() {
        // @AuthenticationPrincipal UserDetails 처리를 위한 리졸버
        HandlerMethodArgumentResolver mockAuthResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(UserDetails.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                UserDetails mockUser = Mockito.mock(UserDetails.class);
                // 컨트롤러에서 userDetails.getUsername()을 쓰므로 가짜 이메일 반환 설정
                given(mockUser.getUsername()).willReturn("test@example.com");
                return mockUser;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(sellerController)
                .setCustomArgumentResolvers(mockAuthResolver)
                .build();
    }

    @Test
    @DisplayName("판매자 신청 API 테스트")
    void applySeller_Success() throws Exception {
        // given
        Seller mockSeller = Mockito.mock(Seller.class);
        given(mockSeller.getBrandName()).willReturn("드랍숍");
        given(mockSeller.getStatus()).willReturn(SellerStatus.PENDING);

        SellerResponse response = new SellerResponse(mockSeller);

        // 첫 번째 인자인 username(String) 매칭을 위해 anyString() 사용
        given(sellerService.applySeller(anyString(), any(SellerApplyRequest.class)))
                .willReturn(response);

        // 하이픈 제거한 10자리 숫자와 필수 필드 포함
        String jsonContent = "{" +
                "\"businessNo\":\"1234567890\"," +
                "\"brandName\":\"드랍숍\"," +
                "\"brandLogoUrl\":\"logo.png\"," +
                "\"accountInfo\":\"신한 110-123\"" +
                "}";

        // when & then
        mockMvc.perform(post("/api/sellers/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandName").value("드랍숍"));
    }

    @Test
    @DisplayName("내 판매자 상태 조회 API 테스트")
    void getMySellerStatus_Success() throws Exception {
        // given
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