package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`column`")
public class ColumnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long columnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User columnAutherId;

    @Column(length = 20)
    private String title;

    private String content;

    private String imageUrl;

    private Integer likeCount;
    private Integer commentCount;
    private Integer scrapCount;

    private boolean isBestColumn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
