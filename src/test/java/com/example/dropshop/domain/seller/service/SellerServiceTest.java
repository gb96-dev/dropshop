package com.example.dropshop.domain.seller.service;

import com.example.dropshop.domain.seller.dto.request.SellerApplyRequest;
import com.example.dropshop.domain.seller.dto.response.SellerResponse;
import com.example.dropshop.domain.seller.entity.Seller;
import com.example.dropshop.domain.seller.enums.SellerStatus;
import com.example.dropshop.domain.seller.repository.SellerRepository;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SellerService sellerService;

    private User testUser;
    private SellerApplyRequest applyRequest;

    @BeforeEach
    void setUp() {
        // User 엔티티의 @Builder를 사용하여 가짜 유저 생성
        testUser = User.signup("test@test.com", "password123!", "테스트닉네임");

        // SellerApplyRequest DTO의 생성자를 사용하여 가짜 요청 생성
        applyRequest = new SellerApplyRequest(
                "회사명",
                "대표자",
                "123-45-67890",
                "010-1234-5678",
                "드랍숍컴퍼니",
                "logo_url",
                "계좌정보"
        );
    }

    @Test
    @DisplayName("판매자 신청 성공 - 새로운 유저가 올바른 정보를 입력했을 때")
    void applySeller_Success() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(testUser));
        given(sellerRepository.existsByBusinessNo(anyString())).willReturn(false);
        given(sellerRepository.findByUser(any())).willReturn(Optional.empty());

        // save 시 인자로 넘어온 Seller 객체를 그대로 반환 (빌더의 기본값 PENDING 확인용)
        given(sellerRepository.save(any(Seller.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        SellerResponse response = sellerService.applySeller("gb650@example.com", applyRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getBrandName()).isEqualTo("드랍숍컴퍼니");
        // 엔티티의 @Builder.Default 덕분에 PENDING이 정상적으로 나옵니다.
        assertThat(response.getStatus()).isEqualTo(SellerStatus.PENDING);
        verify(sellerRepository, times(1)).save(any(Seller.class));
    }

    @Test
    @DisplayName("판매자 신청 실패 - 이미 신청한 내역이 있는 유저")
    void applySeller_Fail_AlreadyApplied() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(testUser));
        given(sellerRepository.findByUser(testUser))
                .willReturn(Optional.of(org.mockito.Mockito.mock(Seller.class)));

        // when & then
        assertThatThrownBy(() -> sellerService.applySeller("gb650@example.com", applyRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 판매자 신청 내역이 존재합니다.");
    }

    @Test
    @DisplayName("판매자 신청 실패 - 중복된 사업자 번호")
    void applySeller_Fail_DuplicateBusinessNo() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(testUser));
        given(sellerRepository.existsByBusinessNo(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> sellerService.applySeller("gb650@example.com", applyRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 등록된 사업자 번호입니다.");
    }

    @Test
    @DisplayName("내 판매자 상태 조회 성공")
    void getMySellerStatus_Success() {
        // given
        Seller seller = Seller.builder()
                .user(testUser)
                .brandName("테스트브랜드")
                .status(SellerStatus.APPROVED)
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(testUser));
        given(sellerRepository.findByUser(testUser)).willReturn(Optional.of(seller));

        // when
        SellerResponse response = sellerService.getMySellerStatus("gb650@example.com");

        // then
        assertThat(response.getStatus()).isEqualTo(SellerStatus.APPROVED);
        assertThat(response.getBrandName()).isEqualTo("테스트브랜드");
    }
}