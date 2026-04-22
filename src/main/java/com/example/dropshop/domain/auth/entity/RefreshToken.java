package com.example.dropshop.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class RefreshToken {

    @Id
    private String email;

    @Column(nullable = false)
    private String token;

    // 튜터님 피드백: 필요한 데이터만 받는 생성자를 직접 정의
    public RefreshToken(String email, String token) {
        this.email = email;
        this.token = token;
    }

    // JPA를 위한 최소한의 기본 생성자 (외부 접근 차단)
    protected RefreshToken() {
    }
}