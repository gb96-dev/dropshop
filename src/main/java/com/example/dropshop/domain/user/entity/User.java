package com.example.dropshop.domain.user.entity;

import com.example.dropshop.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status; // 'user.entity.'를 제거합니다.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // 같은 패키지이므로 직접 참조합니다.

    private LocalDateTime deletedAt;

    /**
     * 사용자 생성 빌더.
     */
    @Builder
    public User(String email, String password, String nickname, UserRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = UserStatus.ACTIVE; //
        this.role = (role != null) ? role : UserRole.USER; //
    }

    /**
     * 비밀번호 변경.
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 회원 탈퇴 (Soft Delete).
     */
    public void withdraw() {
        if (this.status == UserStatus.DELETED) { //
            throw new IllegalStateException("이미 탈퇴된 사용자입니다.");
        }
        this.status = UserStatus.DELETED; //
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 활성 사용자 여부 확인.
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE; //
    }
}