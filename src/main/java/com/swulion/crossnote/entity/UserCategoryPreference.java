package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_category_preference")
public class UserCategoryPreference {

    @EmbeddedId
    private UserCategoryPreferenceId id;

    // id.userId를 실제 user 엔티티와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // UserCategoryPreferenceId의 'userId' 필드에 매핑
    @JoinColumn(name = "user_id")
    private User user;

    // id.categoryId를 실제 category 엔티티와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId") // UserCategoryPreferenceId의 'categoryId' 필드에 매핑
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserCategoryPreference create(User user, Category category, PreferenceType type) {
        UserCategoryPreference preference = new UserCategoryPreference();

        preference.id = new UserCategoryPreferenceId(user.getUserId(), category.getCategoryId(), type);

        preference.user = user;
        preference.category = category;

        return preference;
    }

    public PreferenceType getPreferenceType() {
        return this.id != null ? this.id.getPreferenceType() : null;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}