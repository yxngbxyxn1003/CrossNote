package com.swulion.crossnote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FollowListResponseDto {

    private final Long targetUserId;
    private final List<FollowUserSummaryDto> users;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final int page;
    private final int size;
}

