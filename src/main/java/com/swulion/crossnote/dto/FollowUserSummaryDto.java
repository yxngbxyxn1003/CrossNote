package com.swulion.crossnote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowUserSummaryDto {

    private final Long userId;
    private final String name;
    private final String profileImageUrl;
}

