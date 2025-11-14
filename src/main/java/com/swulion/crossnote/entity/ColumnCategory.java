package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "columnCategory")
public class ColumnCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long columnCategoryId;

    @ManyToOne
    @JoinColumn(name = "columnId")
    private ColumnEntity columnId;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category categoryId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
