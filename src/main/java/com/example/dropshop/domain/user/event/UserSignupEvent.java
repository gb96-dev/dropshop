package com.example.dropshop.domain.user.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회원가입 성공 시 발행되는 Kafka 이벤트. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupEvent {

  private String email;
  private LocalDateTime signupAt;

  public static UserSignupEvent of(String email) {
    return new UserSignupEvent(email, LocalDateTime.now());
  }
}
