package com.swulion.crossnote.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "questionCategory")
public class QuestionCategory {

    @Id
    @GeneratedValue
    private Long questionCategoryId;

    @OneToOne
    @JoinColumn(name = "questionId")
    private Question questionId;

//    @OneToOne
//    @JoinColumn(name = "categoryId")
//    private Category categoryId;

    private LocalDateTime createdAt;
}
