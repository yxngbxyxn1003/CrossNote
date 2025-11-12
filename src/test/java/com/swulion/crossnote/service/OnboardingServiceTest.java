package com.swulion.crossnote.service;

import com.swulion.crossnote.entity.Category;
import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.CategoryRepository;
import com.swulion.crossnote.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(OnboardingService.class)
@Testcontainers
class OnboardingServiceTest {

    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0.40")
            .withDatabaseName("crossnote_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL_CONTAINER::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Category category1;
    private Category category2;
    private Category category3;
    private Category category4;

    @BeforeEach
    void setUp() {
        // 기존 데이터 삭제
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // 사용자 생성
        user = User.builder()
                .email("test@example.com")
                .password("password")
                .name("Tester")
                .loginType(com.swulion.crossnote.entity.LoginType.LOCAL) // 필수
                .build();
        userRepository.save(user);

        // 테스트용 카테고리 생성
        category1 = new Category();
        category1.setCategoryName("Category 1");
        categoryRepository.save(category1);

        category2 = new Category();
        category2.setCategoryName("Category 2");
        categoryRepository.save(category2);

        category3 = new Category();
        category3.setCategoryName("Category 3");
        categoryRepository.save(category3);

        category4 = new Category();
        category4.setCategoryName("Category 4");
        categoryRepository.save(category4);
    }

    @Test
    @DisplayName("사용자 관심분야 저장 테스트 - 중복 없이 저장")
    void saveUserInterest_NoDuplicate() {
        // 1차 저장
        List<Long> categoryIds = List.of(category1.getCategoryId(), category2.getCategoryId(), category3.getCategoryId());
        onboardingService.updateUserInterests(user.getEmail(), categoryIds);

        User refreshed = userRepository.findById(user.getUserId()).orElseThrow();
        List<Long> savedIds = refreshed.getPreferences().stream()
                .filter(p -> p.getPreferenceType() == com.swulion.crossnote.entity.PreferenceType.INTEREST)
                .map(p -> p.getCategory().getCategoryId())
                .collect(Collectors.toList());

        assertThat(savedIds).containsExactlyInAnyOrderElementsOf(categoryIds);

        // 2차 저장 (중복 저장 시 기존 삭제 후 새로 저장)
        List<Long> newCategoryIds = List.of(category2.getCategoryId(), category4.getCategoryId());
        onboardingService.updateUserInterests(user.getEmail(), newCategoryIds);

        refreshed = userRepository.findById(user.getUserId()).orElseThrow();
        savedIds = refreshed.getPreferences().stream()
                .filter(p -> p.getPreferenceType() == com.swulion.crossnote.entity.PreferenceType.INTEREST)
                .map(p -> p.getCategory().getCategoryId())
                .collect(Collectors.toList());

        assertThat(savedIds).containsExactlyInAnyOrderElementsOf(newCategoryIds);
    }
}
