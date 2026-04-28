package com.example.dropshop.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String hashedToken;

    public RefreshToken(String email, String hashedToken) {
        this.email = email;
        this.hashedToken = hashedToken;
    }

    protected RefreshToken() {}

    /**
     * 기존 리프레시 토큰을 갱신한다 (upsert 패턴용).
     */
    public void updateToken(String newHashedToken) {
        this.hashedToken = newHashedToken;
    }
}
