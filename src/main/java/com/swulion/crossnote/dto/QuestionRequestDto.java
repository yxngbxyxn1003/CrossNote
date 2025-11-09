package com.swulion.crossnote.dto;

import lombok.Getter;


public record QuestionRequestDto(Long userId, String title, String content,
                                 String questionCategoryId1, String questionCategoryId2, String questionCategoryId3) {}
