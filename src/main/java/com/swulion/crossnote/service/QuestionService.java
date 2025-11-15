package com.swulion.crossnote.service;

import com.swulion.crossnote.dto.QuestionListDto;
import com.swulion.crossnote.dto.QuestionRequestDto;
import com.swulion.crossnote.dto.QuestionResponseDto;
import com.swulion.crossnote.entity.*;
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
        question.setTitle(questionRequestDto.getTitle());
        question.setContent(questionRequestDto.getContent());
        question.setLikeCount(0);

        User questionerId = userRepository.findById(questionRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        question.setQuestionerId(questionerId);
        questionRepository.save(question);

        Category category1 = categoryRepository.findByCategoryId(questionRequestDto.getCategory1());
        Category category2 = categoryRepository.findByCategoryId(questionRequestDto.getCategory2());
        Category category3 = categoryRepository.findByCategoryId(questionRequestDto.getCategory3());

        QuestionCategory questionCategory1 = new QuestionCategory();
        questionCategory1.setCategoryId(category1);
        questionCategory1.setQuestionId(question);
        questionCategory1.setCreatedAt(LocalDateTime.now());
        questionCategoryRepository.save(questionCategory1);


        QuestionCategory questionCategory2 =  null;
        QuestionCategory questionCategory3 = null;
        if (category2 != null) {
            questionCategory2 = new QuestionCategory();
            questionCategory2.setCategoryId(category2);
            questionCategory2.setQuestionId(question);
            questionCategory2.setCreatedAt(LocalDateTime.now());
            questionCategoryRepository.save(questionCategory2);
        }
        if (category3 != null) {
            questionCategory3 = new QuestionCategory();
            questionCategory3.setCategoryId(category3);
            questionCategory3.setQuestionId(question);
            questionCategory3.setCreatedAt(LocalDateTime.now());
            questionCategoryRepository.save(questionCategory3);
        }

        Long responseCatId2 = (category2 != null) ? category2.getCategoryId() : null;
        Long responseCatId3 = (category3 != null) ? category3.getCategoryId() : null;

        return new QuestionResponseDto(questionerId.getUserId(), question.getTitle(), question.getContent(),
                question.getLikeCount(), question.getCreatedAt(), question.getUpdatedAt(), category1.getCategoryId(), responseCatId2, responseCatId3);

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
