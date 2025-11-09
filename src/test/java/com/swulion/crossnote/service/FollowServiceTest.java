package com.swulion.crossnote.service;

import com.swulion.crossnote.dto.FollowListResponseDto;
import com.swulion.crossnote.dto.FollowStatusResponseDto;
import com.swulion.crossnote.entity.LoginType;
import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.FollowRepository;
import com.swulion.crossnote.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(FollowService.class)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FollowServiceTest {

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
    private FollowService followService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    private User follower;
    private User followee;

    @BeforeEach
    void setUp() {
        followRepository.deleteAll();
        userRepository.deleteAll();

        follower = createUser("follower@example.com", "Follower");
        followee = createUser("followee@example.com", "Followee");
    }

    @Test
    @DisplayName("팔로우 시 팔로우 관계가 생성되고 팔로워/팔로잉 수가 증가한다")
    void follow_createsRelationshipAndIncrementsCounts() {
        FollowStatusResponseDto response = followService.follow(follower.getUserId(), followee.getUserId());

        assertThat(response.isFollowing()).isTrue();
        assertThat(response.getFollowerCount()).isEqualTo(1);
        assertThat(response.getFollowingCount()).isZero();

        User refreshedFollower = userRepository.findById(follower.getUserId()).orElseThrow();
        User refreshedFollowee = userRepository.findById(followee.getUserId()).orElseThrow();

        assertThat(refreshedFollower.getFollowingsCount()).isEqualTo(1);
        assertThat(refreshedFollowee.getFollowersCount()).isEqualTo(1);
        assertThat(followRepository.existsByFollowerAndFollowee(refreshedFollower, refreshedFollowee)).isTrue();
    }

    @Test
    @DisplayName("이미 팔로우 중이면 중복 생성 없이 상태만 반환한다")
    void follow_whenDuplicate_requestIsIdempotent() {
        followService.follow(follower.getUserId(), followee.getUserId());
        FollowStatusResponseDto response = followService.follow(follower.getUserId(), followee.getUserId());

        assertThat(response.isFollowing()).isTrue();
        assertThat(followRepository.count()).isEqualTo(1);

        User refreshedFollowee = userRepository.findById(followee.getUserId()).orElseThrow();
        assertThat(refreshedFollowee.getFollowersCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("언팔로우 시 팔로우 관계가 삭제되고 카운트가 감소한다")
    void unfollow_removesRelationshipAndDecrementsCounts() {
        followService.follow(follower.getUserId(), followee.getUserId());

        FollowStatusResponseDto response = followService.unfollow(follower.getUserId(), followee.getUserId());

        assertThat(response.isFollowing()).isFalse();
        assertThat(followRepository.count()).isZero();

        User refreshedFollower = userRepository.findById(follower.getUserId()).orElseThrow();
        User refreshedFollowee = userRepository.findById(followee.getUserId()).orElseThrow();

        assertThat(refreshedFollower.getFollowingsCount()).isZero();
        assertThat(refreshedFollowee.getFollowersCount()).isZero();
    }

    @Test
    @DisplayName("팔로워 목록 조회 시 페이지 형태로 반환된다")
    void getFollowers_returnsPagedResponse() {
        User follower2 = createUser("follower2@example.com", "Follower2");
        User follower3 = createUser("follower3@example.com", "Follower3");

        followService.follow(follower.getUserId(), followee.getUserId());
        followService.follow(follower2.getUserId(), followee.getUserId());
        followService.follow(follower3.getUserId(), followee.getUserId());

        FollowListResponseDto page0 = followService.getFollowers(followee.getUserId(), PageRequest.of(0, 2));

        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getUsers()).hasSize(2);
        assertThat(page0.isHasNext()).isTrue();

        FollowListResponseDto page1 = followService.getFollowers(followee.getUserId(), PageRequest.of(1, 2));

        assertThat(page1.getUsers()).hasSize(1);
        assertThat(page1.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("팔로잉 목록 조회 시 페이지 형태로 반환된다")
    void getFollowings_returnsPagedResponse() {
        User followee2 = createUser("followee2@example.com", "Followee2");
        User followee3 = createUser("followee3@example.com", "Followee3");

        followService.follow(follower.getUserId(), followee.getUserId());
        followService.follow(follower.getUserId(), followee2.getUserId());
        followService.follow(follower.getUserId(), followee3.getUserId());

        FollowListResponseDto page0 = followService.getFollowings(follower.getUserId(), PageRequest.of(0, 2));

        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getUsers()).hasSize(2);
        assertThat(page0.isHasNext()).isTrue();
    }

    private User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .password("password")
                .name(name)
                .loginType(LoginType.LOCAL)
                .build();
        return userRepository.save(user);
    }
}

