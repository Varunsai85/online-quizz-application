package org.varun.onlinequizzapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.varun.onlinequizzapp.service.QuizAttemptService;

@RestController
@RequestMapping("api/attempt")
@RequiredArgsConstructor
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;

    @GetMapping("quiz/{quizId}")
    public ResponseEntity<?> getAttemptFromUser(@PathVariable Long quizId) {
        return quizAttemptService.getAttemptsFromQuizAndUserId(quizId);
    }
}
