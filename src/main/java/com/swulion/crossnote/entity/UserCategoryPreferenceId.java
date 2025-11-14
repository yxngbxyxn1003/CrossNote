package com.swulion.crossnote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙을 위한 기본 생성자
public class UserCategoryPreferenceId implements Serializable {

    @Column(name = "user_id") // @MapsId를 사용하므로 실제론 필요 없으나, 명시성을 위해 추가
    private Long userId;

    @Column(name = "category_id") // @MapsId를 사용하므로 실제론 필요 없으나, 명시성을 위해 추가
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference_type")
    private PreferenceType preferenceType;

    public UserCategoryPreferenceId(Long userId, Long categoryId, PreferenceType preferenceType) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.preferenceType = preferenceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCategoryPreferenceId that)) return false;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(categoryId, that.categoryId) &&
                preferenceType == that.preferenceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, categoryId, preferenceType);
    }
}