package com.example.dropshop.domain.user.dto.response;

import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.entity.UserRole;
import com.example.dropshop.domain.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 응답 DTO.
 */
@Getter
@Builder
public class UserResponse {

    private String email;
    private String nickname;
    private UserStatus status;
    private UserRole role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .status(user.getStatus())
                .role(user.getRole())
                .build();
    }
}
