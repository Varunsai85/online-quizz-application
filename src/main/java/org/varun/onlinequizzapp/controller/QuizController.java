package org.varun.onlinequizzapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.varun.onlinequizzapp.dto.quiz.AddQuizDto;
import org.varun.onlinequizzapp.service.QuizService;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping("quizzes")
    public ResponseEntity<?> getAllQuizzes(){
        return quizService.getAllQuizzes();
    }

    @PostMapping("quiz")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addQuiz(@Valid @RequestBody AddQuizDto input) {
        return quizService.addQuiz(input);
    }
}
