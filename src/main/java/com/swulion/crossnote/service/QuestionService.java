package com.swulion.crossnote.service;

import com.swulion.crossnote.dto.QuestionListDto;
import com.swulion.crossnote.dto.QuestionRequestDto;
import com.swulion.crossnote.dto.QuestionResponseDto;
import com.swulion.crossnote.entity.Answer;
import com.swulion.crossnote.entity.Question;
import com.swulion.crossnote.entity.QuestionCategory;
import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AnswerRepository answerRepository;
    private final QuestionCategoryRepository questionCategoryRepository;

    /* 질문 생성 로직 */
    public QuestionResponseDto createQuestion(QuestionRequestDto questionRequestDto) {
        Question question = new Question();
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setTitle(questionRequestDto.title());
        question.setContent(questionRequestDto.content());
        question.setLikeCount(0);

        User questionerId = userRepository.findById(questionRequestDto.userId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        question.setQuestionerId(questionerId);
        questionRepository.save(question);

        Long questionCategoryId1 = categoryRepository.findByCategoryName(questionRequestDto.questionCategoryId1());
        Long questionCategoryId2 = categoryRepository.findByCategoryName(questionRequestDto.questionCategoryId2());
        Long questionCategoryId3 = categoryRepository.findByCategoryName(questionRequestDto.questionCategoryId3());

        QuestionCategory questionCategory1 = new QuestionCategory();
        questionCategory1.setQuestionCategoryId(questionCategoryId1);
        questionCategory1.setQuestionId(question);
        questionCategory1.setCreatedAt(LocalDateTime.now());
        questionCategoryRepository.save(questionCategory1);

        if (questionCategoryId2 != null) {
            QuestionCategory questionCategory2 = new QuestionCategory();
            questionCategory2.setQuestionCategoryId(questionCategoryId2);
            questionCategory2.setQuestionId(question);
            questionCategory2.setCreatedAt(LocalDateTime.now());
            questionCategoryRepository.save(questionCategory2);
        }
        if (questionCategoryId3 != null) {
            QuestionCategory questionCategory3 = new QuestionCategory();
            questionCategory3.setQuestionCategoryId(questionCategoryId3);
            questionCategory3.setQuestionId(question);
            questionCategory3.setCreatedAt(LocalDateTime.now());
            questionCategoryRepository.save(questionCategory3);
        }

        return new QuestionResponseDto(questionerId.getUserId(), question.getTitle(), question.getContent(),
                0, question.getCreatedAt(), question.getUpdatedAt(), questionCategoryId1, questionCategoryId2, questionCategoryId3);

    }

    /* Question 전체 보기 (홈) */
    public List<QuestionListDto> getQnaHome(){
        List<Question> questions = questionRepository.findAll();
        List<QuestionListDto> questionListDtos = new ArrayList<>();
        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionId(question);
            questionListDtos.add(
                    new QuestionListDto(
                            question.getQuestionId(),
                            question.getTitle(),
                            question.getContent(),
                            question.getLikeCount(),
                            answers.size()
                    )
            );
        }
        return questionListDtos;
    }
}
