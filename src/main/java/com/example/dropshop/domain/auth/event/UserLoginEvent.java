package com.example.dropshop.domain.auth.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 성공 시 발행되는 Kafka 이벤트.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginEvent {

    private String email;
    private LocalDateTime loginAt;

    public static UserLoginEvent of(String email) {
        return new UserLoginEvent(email, LocalDateTime.now());
    }
}
