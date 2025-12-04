package org.varun.onlinequizzapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.varun.onlinequizzapp.dto.quiz.AddQuizDto;
import org.varun.onlinequizzapp.dto.quiz.UpdateQuizDto;
import org.varun.onlinequizzapp.service.QuizService;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping("quizzes")
    public ResponseEntity<?> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    @GetMapping("quiz/{id}")
    public ResponseEntity<?> getQuizWithId(@PathVariable Long id){
        return quizService.getQuizWithId(id);
    }

    @PostMapping("quiz")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addQuiz(@Valid @RequestBody AddQuizDto input) {
        return quizService.addQuiz(input);
    }

    @DeleteMapping("quiz/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id){
        return quizService.deleteQuiz(id);
    }

    @PatchMapping("quiz/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateQuiz(@PathVariable Long id, @Valid @RequestBody UpdateQuizDto input){
        return quizService.updateQuiz(id,input);
    }
}
