package com.swulion.crossnote.dto;

import java.time.LocalDateTime;


public record QuestionResponseDto (
        Long questionerId, String title, String content, Integer likeCount,
        LocalDateTime createdAt, LocalDateTime updatedAt,
        Long questionCategoryId1, Long questionCategoryId2, Long questionCategoryId3) {

}