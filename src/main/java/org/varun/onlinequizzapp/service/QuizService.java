package org.varun.onlinequizzapp.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.quiz.AddQuizDto;
import org.varun.onlinequizzapp.dto.quiz.QuizResponseDto;
import org.varun.onlinequizzapp.model.Quiz;
import org.varun.onlinequizzapp.model.Topic;
import org.varun.onlinequizzapp.repository.QuizRepository;
import org.varun.onlinequizzapp.repository.TopicRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {
    private final QuizRepository quizRepo;
    private final TopicRepository topicRepo;

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllQuizzes() {
        try {
            List<Quiz> quizzes = quizRepo.findAll();
            List<QuizResponseDto> responses = quizzes.stream().map(quiz -> new QuizResponseDto(quiz.getId(),
                    quiz.getTitle(),
                    quiz.getDescription(),
                    quiz.getTopic().getName(),
                    quiz.getTimeLimitMinutes(),
                    quiz.getDifficultyLevel(),
                    quiz.getCreatedAt())).toList();
            return new ResponseEntity<>(new ApiResponse<>("All quizzes fetched successfully", responses), HttpStatus.OK);
        } catch (Exception e) {
            log.error("[Get-Quizzes] Encounter error while fetching quizzes: {}", e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> addQuiz(@Valid AddQuizDto input) {
        try {
            Topic topic = topicRepo.findById(input.topicId()).orElseThrow(() -> new RuntimeException("Topic with id " + input.topicId() + " not found"));
            if (quizRepo.existsQuizByTitleIgnoreCase(input.title().trim())) {
                return new ResponseEntity<>(new ApiResponse<>("Quiz with the title already exists"), HttpStatus.CONFLICT);
            }
            Quiz newQuiz = Quiz.builder()
                    .title(input.title().trim())
                    .description(input.description().trim())
                    .topic(topic)
                    .timeLimitMinutes(input.timeLimit())
                    .difficultyLevel(input.difficulty())
                    .build();
            quizRepo.saveAndFlush(newQuiz);
            return new ResponseEntity<>(new ApiResponse<>("Quiz created successfully"),HttpStatus.CREATED);
        }catch (Exception e){
            log.error("[Add-Quiz] Error encountered while adding new quiz: {}",e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong",e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
