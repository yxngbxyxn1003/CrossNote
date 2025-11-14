package com.swulion.crossnote.service;

import com.swulion.crossnote.dto.Onboarding.OnboardingDetailResponseDto;
import com.swulion.crossnote.dto.Onboarding.OnboardingGoogleRequestDto;
import com.swulion.crossnote.dto.Onboarding.OnboardingResponseDto;
import com.swulion.crossnote.entity.*;
import com.swulion.crossnote.repository.CategoryRepository;
import com.swulion.crossnote.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.*;

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
    private Category parentCategory;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        user = User.builder()
                .email("test@example.com")
                .password("password")
                .name("Tester")
                .loginType(LoginType.LOCAL)
                .curationLevel(CurationLevel.LEVEL_1)
                .build();
        userRepository.save(user);

        parentCategory = new Category();
        parentCategory.setCategoryName("ParentCategory");
        parentCategory.setParentCategoryId(null);
        categoryRepository.save(parentCategory);

        category1 = new Category();
        category1.setCategoryName("Category 1");
        category1.setParentCategoryId(parentCategory);
        categoryRepository.save(category1);

        category2 = new Category();
        category2.setCategoryName("Category 2");
        category2.setParentCategoryId(parentCategory);
        categoryRepository.save(category2);

        category3 = new Category();
        category3.setCategoryName("Category 3");
        category3.setParentCategoryId(parentCategory);
        categoryRepository.save(category3);

        category4 = new Category();
        category4.setCategoryName("Category 4");
        category4.setParentCategoryId(parentCategory);
        categoryRepository.save(category4);
    }

    @Autowired
    private EntityManager entityManager;
    @Test
    @DisplayName("사용자 관심분야 저장 테스트 - 중복 없이 저장")
    @Transactional
    void updateUserInterests_NoDuplicate() {
        List<String> categoryNames = List.of(category1.getCategoryName(), category2.getCategoryName(), category3.getCategoryName());

        User managedUser = userRepository.findByEmail(user.getEmail()).orElseThrow();

        List<Category> categories = categoryNames.stream()
                .map(categoryRepository::findByCategoryName)
                .collect(Collectors.toList());

        Set<Long> existingCatIds = managedUser.getPreferences().stream()
                .filter(p -> p.getPreferenceType() == PreferenceType.INTEREST)
                .map(p -> p.getCategory().getCategoryId())
                .collect(toSet());

        managedUser.getPreferences().removeIf(p ->
                p.getPreferenceType() == PreferenceType.INTEREST && !categories.stream()
                        .map(Category::getCategoryId).collect(toSet())
                        .contains(p.getCategory().getCategoryId())
        );
        userRepository.save(managedUser);
        userRepository.flush();
        entityManager.clear();

        for (Category cat : categories) {
            if (!existingCatIds.contains(cat.getCategoryId())) {
                UserCategoryPreference preference = UserCategoryPreference.create(managedUser, cat, PreferenceType.INTEREST);
                managedUser.getPreferences().add(preference);
            }
        }
        userRepository.save(managedUser);
        userRepository.flush();
        entityManager.clear();

        User refreshed = userRepository.findByEmail(user.getEmail()).orElseThrow();
        Set<String> savedNames = refreshed.getPreferences().stream()
                .filter(p -> p.getPreferenceType() == PreferenceType.INTEREST)
                .map(p -> p.getCategory().getCategoryName())
                .collect(Collectors.toSet());

        assertThat(savedNames).containsExactlyInAnyOrder(
                category1.getCategoryName(),
                category2.getCategoryName(),
                category3.getCategoryName()
        );
    }

    @Test
    @DisplayName("사용자 전문분야 저장 테스트 - 상위 카테고리 선택 시 예외")
    void saveUserExpertise_UpperCategoryException() {
        List<String> expertiseNames = List.of(parentCategory.getCategoryName());
        assertThatThrownBy(() -> onboardingService.updateUserExpertise(user.getEmail(), expertiseNames))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상위 카테고리는 전문분야로 선택할 수 없습니다.");
    }

    @Test
    @DisplayName("구글 기본정보 업데이트 테스트")
    void updateGoogleBasicInfo_Success() {
        OnboardingGoogleRequestDto dto = new OnboardingGoogleRequestDto();
        dto.setGender(Gender.MALE);
        dto.setBirthdate(LocalDate.of(1990, 1, 1));

        OnboardingResponseDto response = onboardingService.updateGoogleBasicInfo(user.getEmail(), dto);

        User refreshed = userRepository.findById(user.getUserId()).orElseThrow();
        assertThat(refreshed.getGender()).isEqualTo(Gender.MALE);
        assertThat(refreshed.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(response.getUserId()).isEqualTo(user.getUserId());
    }

    @Test
    @DisplayName("큐레이션 레벨 업데이트 테스트")
    void updateCurationLevel_Success() {
        OnboardingResponseDto response = onboardingService.updateCurationLevel(user.getEmail(), CurationLevel.LEVEL_2);

        User refreshed = userRepository.findById(user.getUserId()).orElseThrow();
        assertThat(refreshed.getCurationLevel()).isEqualTo(CurationLevel.LEVEL_2);
        assertThat(response.getUserId()).isEqualTo(user.getUserId());
    }

    @Test
    @DisplayName("온보딩 상세 정보 조회 테스트")
    void getUserOnboardingInfo_Success() {
        // 세팅: 관심분야 + 전문분야 등록
        onboardingService.updateUserInterests(user.getEmail(), List.of(category1.getCategoryName(), category2.getCategoryName()));
        onboardingService.updateUserExpertise(user.getEmail(), List.of(category3.getCategoryName()));

        OnboardingDetailResponseDto dto = onboardingService.getUserOnboardingInfo(user.getUserId());

        assertThat(dto.getInterestNames()).containsExactlyInAnyOrder(category1.getCategoryName(), category2.getCategoryName());
        assertThat(dto.getExpertiseNames()).containsExactly(category3.getCategoryName());
        assertThat(dto.getCurationLevel()).isEqualTo(user.getCurationLevel());
    }
}
