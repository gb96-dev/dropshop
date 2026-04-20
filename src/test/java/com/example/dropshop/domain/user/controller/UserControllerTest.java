package com.example.dropshop.domain.user.controller;

import com.example.dropshop.domain.user.dto.request.PasswordUpdateRequest;
import com.example.dropshop.domain.user.dto.request.SignupRequest;
import com.example.dropshop.domain.user.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    @WithMockUser(username = "test@test.com")
    void updatePassword_Success() throws Exception {
        PasswordUpdateRequest request = new PasswordUpdateRequest("oldPass123!", "newPass123!");

        willDoNothing().given(userService).updatePassword(anyString(), any());

        mockMvc.perform(patch("/api/users/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    @WithMockUser(username = "test@test.com")
    void withdraw_Success() throws Exception {
        willDoNothing().given(userService).withdraw(anyString());

        mockMvc.perform(delete("/api/users/withdraw")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() throws Exception {
        SignupRequest request = new SignupRequest("test@test.com", "password123!", "규범");
        willDoNothing().given(userService).signup(any());

        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}