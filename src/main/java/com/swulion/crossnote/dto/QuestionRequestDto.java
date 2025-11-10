package com.swulion.crossnote.dto;

import com.swulion.crossnote.entity.QuestionCategory;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionRequestDto {
    private Long userId;
    private String title;
    private String content;
    @Column(nullable = false)
    private Long category1;
    @Column(nullable = true)
    private Long category2;
    @Column(nullable = true)
    private Long category3;
}
