package com.swulion.crossnote.dto;

public record QuestionListDto(Long questionId, String title, String content, Integer likeCount, Integer answerCount) {
}
