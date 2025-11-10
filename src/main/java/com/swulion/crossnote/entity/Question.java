package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User questionerId;

    @Column(length = 100)
    private String title;

    @Column(length = 1000)
    private String content;

    private Integer likeCount;

//    private boolean isTodayQna;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
