package com.swulion.crossnote.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ColumnReadResponseDto {
    Long columnId;
    Long authorId;
    String title;
    Boolean isBestColumn;
    Integer likeCount;
    Integer commentCount;
    Long categoryId1;
    Long categoryId2;
    Long categoryId3;
}
