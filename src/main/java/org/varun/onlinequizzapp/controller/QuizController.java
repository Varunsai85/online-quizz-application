package org.varun.onlinequizzapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
