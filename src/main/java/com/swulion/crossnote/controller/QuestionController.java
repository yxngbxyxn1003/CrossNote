package com.swulion.crossnote.controller;

import com.swulion.crossnote.dto.QuestionListDto;
import com.swulion.crossnote.dto.QuestionRequestDto;
import com.swulion.crossnote.dto.QuestionResponseDto;
import com.swulion.crossnote.entity.Question;
import com.swulion.crossnote.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/question")
public class QuestionController {

    private final QuestionService questionService;

    /* 질문 생성 */
    @PostMapping("/create")
    public ResponseEntity<QuestionResponseDto> createQuestion(@RequestBody QuestionRequestDto questionRequestDto) {
        QuestionResponseDto questionResponseDto = questionService.createQuestion(questionRequestDto);
        return ResponseEntity.ok(questionResponseDto);
    }

    /* QNA 홈 */
    @GetMapping("/home")
    public ResponseEntity<List<QuestionListDto>> getQuestionHome(){
        List<QuestionListDto> questionListDtos = questionService.getQnaHome();
        return ResponseEntity.ok(questionListDtos);
    }
}
