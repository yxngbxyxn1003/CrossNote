package com.swulion.crossnote.service;

import com.swulion.crossnote.dto.FollowListResponseDto;
import com.swulion.crossnote.dto.FollowStatusResponseDto;
import com.swulion.crossnote.dto.FollowUserSummaryDto;
import com.swulion.crossnote.entity.Follow;
import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.FollowRepository;
import com.swulion.crossnote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FollowStatusResponseDto getFollowStatus(Long currentUserId, Long targetUserId) {
        User currentUser = findUserById(currentUserId);
        User targetUser = findUserById(targetUserId);

        boolean following = followRepository.existsByFollowerAndFollowee(currentUser, targetUser);
        return buildStatusResponse(targetUser, following);
    }

    public FollowStatusResponseDto follow(Long currentUserId, Long targetUserId) {
        validateDifferentUsers(currentUserId, targetUserId, "자기 자신은 팔로우할 수 없습니다.");

        User follower = findUserById(currentUserId);
        User followee = findUserById(targetUserId);

        if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
            return buildStatusResponse(followee, true);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

        followRepository.save(follow);

        follower.increaseFollowingsCount();
        followee.increaseFollowersCount();

        return buildStatusResponse(followee, true);
    }

    public FollowStatusResponseDto unfollow(Long currentUserId, Long targetUserId) {
        validateDifferentUsers(currentUserId, targetUserId, "자기 자신은 팔로우 취소할 수 없습니다.");

        User follower = findUserById(currentUserId);
        User followee = findUserById(targetUserId);

        Follow follow = followRepository.findByFollowerAndFollowee(follower, followee)
                .orElseThrow(() -> new IllegalStateException("팔로우 관계가 존재하지 않습니다."));

        followRepository.delete(follow);

        follower.decreaseFollowingsCount();
        followee.decreaseFollowersCount();

        return buildStatusResponse(followee, false);
    }

    @Transactional(readOnly = true)
    public FollowListResponseDto getFollowers(Long targetUserId, Pageable pageable) {
        User targetUser = findUserById(targetUserId);

        Page<Follow> page = followRepository.findAllByFollowee(targetUser, pageable);
        List<FollowUserSummaryDto> users = page.getContent().stream()
                .map(follow -> toUserSummaryDto(follow.getFollower()))
                .collect(Collectors.toList());

        return buildListResponse(targetUserId, pageable, page, users);
    }

    @Transactional(readOnly = true)
    public FollowListResponseDto getFollowings(Long targetUserId, Pageable pageable) {
        User targetUser = findUserById(targetUserId);

        Page<Follow> page = followRepository.findAllByFollower(targetUser, pageable);
        List<FollowUserSummaryDto> users = page.getContent().stream()
                .map(follow -> toUserSummaryDto(follow.getFollowee()))
                .collect(Collectors.toList());

        return buildListResponse(targetUserId, pageable, page, users);
    }

    private FollowStatusResponseDto buildStatusResponse(User targetUser, boolean following) {
        return new FollowStatusResponseDto(
                targetUser.getUserId(),
                following,
                targetUser.getFollowersCount(),
                targetUser.getFollowingsCount()
        );
    }

    private FollowListResponseDto buildListResponse(Long targetUserId,
                                                    Pageable pageable,
                                                    Page<Follow> page,
                                                    List<FollowUserSummaryDto> users) {
        return new FollowListResponseDto(
                targetUserId,
                users,
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }

    private FollowUserSummaryDto toUserSummaryDto(User user) {
        return new FollowUserSummaryDto(
                user.getUserId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId=" + userId));
    }

    private void validateDifferentUsers(Long currentUserId, Long targetUserId, String message) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException(message);
        }
    }
}

