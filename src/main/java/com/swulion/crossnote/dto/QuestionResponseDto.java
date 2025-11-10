package com.swulion.crossnote.dto;

import java.time.LocalDateTime;


public record QuestionResponseDto (
        Long questionerId, String title, String content, Integer likeCount,
        LocalDateTime createdAt, LocalDateTime updatedAt,
        Long categoryId1, Long categoryId2, Long categoryId3) {

}