package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "parentCategoryId")
    private Category parentCategoryId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
