package com.example.dropshop.domain.user.controller;

import com.example.dropshop.common.dto.ApiResponse;
import com.example.dropshop.domain.user.dto.SignupRequest;
import com.example.dropshop.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API (POST /api/users/signup)
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(null));
    }
}