package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "column")
public class ColumnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long columnId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User columnAutherId;

    // 카테고리 최대 3개라서 하나는 not null, 두 개는 nullable하게 기본값 설정하도록 DB 수정했습니다.
//    @ManyToOne
//    @JoinColumn(name  = "categoryId")
//    @Column(nullable = false)
//    private Category categoryId1;

//    @ManyToOne
//    @JoinColumn(name  = "categoryId")
//    @Column(nullable = true)
//    private Category categoryId2;

//    @ManyToOne
//    @JoinColumn(name  = "categoryId")
//    @Column(nullable = true)
//    private Category categoryId3;


    @Column(length = 100)
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
