package com.swulion.crossnote.controller;

import com.swulion.crossnote.dto.FollowListResponseDto;
import com.swulion.crossnote.dto.FollowStatusResponseDto;
import com.swulion.crossnote.service.CustomUserDetails;
import com.swulion.crossnote.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class FollowController {

    private final FollowService followService;

    @GetMapping("/{targetUserId}/follow-status")
    public ResponseEntity<FollowStatusResponseDto> getFollowStatus(
            @PathVariable("targetUserId") Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = extractUserId(userDetails);
        FollowStatusResponseDto response = followService.getFollowStatus(currentUserId, targetUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{targetUserId}/follow")
    public ResponseEntity<FollowStatusResponseDto> follow(
            @PathVariable("targetUserId") Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = extractUserId(userDetails);
        FollowStatusResponseDto response = followService.follow(currentUserId, targetUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{targetUserId}/follow")
    public ResponseEntity<FollowStatusResponseDto> unfollow(
            @PathVariable("targetUserId") Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = extractUserId(userDetails);
        FollowStatusResponseDto response = followService.unfollow(currentUserId, targetUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{targetUserId}/followers")
    public ResponseEntity<FollowListResponseDto> getFollowers(
            @PathVariable("targetUserId") Long targetUserId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = buildPageable(page, size);
        FollowListResponseDto response = followService.getFollowers(targetUserId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{targetUserId}/followings")
    public ResponseEntity<FollowListResponseDto> getFollowings(
            @PathVariable("targetUserId") Long targetUserId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = buildPageable(page, size);
        FollowListResponseDto response = followService.getFollowings(targetUserId, pageable);
        return ResponseEntity.ok(response);
    }

    private Long extractUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new IllegalArgumentException("인증된 사용자만 접근할 수 있습니다.");
        }
        return userDetails.getUser().getUserId();
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : size;
        safeSize = Math.min(safeSize, 50);
        return PageRequest.of(safePage, safeSize);
    }
}

