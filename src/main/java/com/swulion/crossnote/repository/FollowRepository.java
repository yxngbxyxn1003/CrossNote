package com.swulion.crossnote.repository;

import com.swulion.crossnote.entity.Follow;
import com.swulion.crossnote.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowee(User follower, User followee);

    Optional<Follow> findByFollowerAndFollowee(User follower, User followee);

    long countByFollower(User follower);

    long countByFollowee(User followee);

    Page<Follow> findAllByFollower(User follower, Pageable pageable);

    Page<Follow> findAllByFollowee(User followee, Pageable pageable);
}

