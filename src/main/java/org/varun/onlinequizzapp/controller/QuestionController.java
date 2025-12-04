package org.varun.onlinequizzapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.varun.onlinequizzapp.dto.question.AddQuestionDto;
import org.varun.onlinequizzapp.service.QuestionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api")
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("questions")
    public ResponseEntity<?> getAllQuestions(){
        return questionService.getAllQuestions();
    }

    @PostMapping("question")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addQuestion(@Valid @RequestBody AddQuestionDto input){
        return questionService.addQuestion(input);
    }
}
