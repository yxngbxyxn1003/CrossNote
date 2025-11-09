package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* 유저 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false, length = 100)
    private String email; // 로그인 ID로 사용

    @Column(length = 100)
    private String password; // 소셜 로그인에서 이 필드는 NULL이 됨

    @Column(nullable = false, length = 50)
    private String name;

    private LocalDateTime birthDate;

    @Column(length = 255)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    @Column(nullable = false)
    private Integer curationLevel;

    @Column(nullable = false)
    private long followersCount = 0L;

    @Column(nullable = false)
    private long followingsCount = 0L;

    /* Builder */
    @Builder
    public User(String email, String password, String name, LocalDateTime birthDate,
                String profileImageUrl, LoginType loginType, Integer curationLevel,
                Long followersCount, Long followingsCount) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.profileImageUrl = profileImageUrl;
        this.loginType = loginType;
        // UserService에서 curationLevel을 1로 설정했으므로, 빌더에서도 기본값 처리
        this.curationLevel = (curationLevel != null) ? curationLevel : 1;
        this.followersCount = (followersCount != null) ? followersCount : 0L;
        this.followingsCount = (followingsCount != null) ? followingsCount : 0L;
    }

    /* 소셜 로그인 시, 기존 유저의 정보가 변경되었을 때 업데이트 */
    public User updateSocialInfo(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public void increaseFollowersCount() {
        this.followersCount++;
    }

    public void decreaseFollowersCount() {
        if (this.followersCount > 0) {
            this.followersCount--;
        }
    }

    public void increaseFollowingsCount() {
        this.followingsCount++;
    }

    public void decreaseFollowingsCount() {
        if (this.followingsCount > 0) {
            this.followingsCount--;
        }
    }
}
