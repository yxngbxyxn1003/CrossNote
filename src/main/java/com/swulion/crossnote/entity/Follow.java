package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "follow",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follow_follower_followee",
                        columnNames = {"follower_id", "followee_id"}
                )
        }
)
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followee_id", nullable = false)
    private User followee;

    @Builder
    public Follow(User follower, User followee) {
        if (follower == null || followee == null) {
            throw new IllegalArgumentException("팔로우 관계를 생성하려면 유효한 사용자 정보가 필요합니다.");
        }
        this.follower = follower;
        this.followee = followee;
    }
}

