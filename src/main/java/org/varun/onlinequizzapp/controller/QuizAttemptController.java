package org.varun.onlinequizzapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.varun.onlinequizzapp.dto.quizAttempt.SubmitAnswerDto;
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

    @PostMapping("quiz/{quizId}")
    public ResponseEntity<?> attemptQuiz(@PathVariable Long quizId) {
        return quizAttemptService.addAttempt(quizId);
    }

    @PostMapping("answer/{attemptId}")
    public ResponseEntity<?> submitAnswer(@PathVariable Long attemptId, @Valid SubmitAnswerDto input) {
        return quizAttemptService.submitAnswer(attemptId, input);
    }
}
