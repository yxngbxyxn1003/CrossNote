package com.swulion.crossnote.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/* 유저 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(unique = true, nullable = false, length = 100)
    private String email; // 로그인 ID로 사용

    @Column(length = 255)
    private String password; // 소셜 로그인에서 이 필드는 NULL이 됨

    @Column(nullable = false, length = 50)
    private String name;

    @Column
    private LocalDate birthDate;

    @Column(length = 255)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    // 큐레이션 레벨 - Enum 타입으로 통일
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "curation_level", length = 20)
    private CurationLevel curationLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(nullable = false)
    private long followersCount = 0L;

    @Column(nullable = false)
    private long followingsCount = 0L;

    /* 온보딩 연관관계 (User 1 : N UserCategoryPreference) */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserCategoryPreference> preferences = new HashSet<>();

    /* Builder */
    @Builder
    public User(String email, String password, String name, LocalDate birthDate,
                String profileImageUrl, LoginType loginType,
                Gender gender, CurationLevel curationLevel,
                Long followersCount, Long followingsCount) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.profileImageUrl = profileImageUrl;
        this.loginType = loginType;
        this.gender = gender;
        this.curationLevel = curationLevel;
        this.followersCount = (followersCount != null) ? followersCount : 0L;
        this.followingsCount = (followingsCount != null) ? followingsCount : 0L;
    }

    /* 소셜 로그인 시, 기존 유저의 정보가 변경되었을 때 업데이트 */
    public User updateSocialInfo(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public void updateOnboardingGoogle(Gender gender, LocalDate localDate) {
        this.gender = gender;
        this.birthDate = localDate;
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
