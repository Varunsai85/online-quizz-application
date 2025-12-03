package org.varun.onlinequizzapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.quiz.QuizResponseDto;
import org.varun.onlinequizzapp.model.Quiz;
import org.varun.onlinequizzapp.repository.QuizRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepo;

    @Transactional
    public ResponseEntity<?> getAllQuizzes() {
        List<Quiz> quizzes=quizRepo.findAll();
        List<QuizResponseDto> responses=quizzes.stream().map(quiz->new QuizResponseDto(quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTopic().getName(),
                quiz.getTimeLimitMinutes(),
                quiz.getDifficultyLevel(),
                quiz.getCreatedAt())).toList();
        return new ResponseEntity<>(new ApiResponse<>("All quizzes fetched successfully",responses), HttpStatus.OK);
    }
}
