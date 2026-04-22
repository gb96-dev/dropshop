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
    private String hashedToken;

    public RefreshToken(String email, String hashedToken) {
        this.email = email;
        this.hashedToken = hashedToken;
    }

    protected RefreshToken() {}
}