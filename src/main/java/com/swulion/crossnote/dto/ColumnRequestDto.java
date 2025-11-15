package com.swulion.crossnote.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColumnRequestDto {
    //private Long userId;
    private String title;
    private String content;
    @Column(nullable = false)
    private Long category1;
    private Long category2;
    private Long category3;
    private String imageUrl;
}
