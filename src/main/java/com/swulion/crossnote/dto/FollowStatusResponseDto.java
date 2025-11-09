package com.swulion.crossnote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowStatusResponseDto {

    private final Long targetUserId;
    private final boolean following;
    private final long followerCount;
    private final long followingCount;
}

