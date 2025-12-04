package org.varun.onlinequizzapp.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.quiz.AddQuizDto;
import org.varun.onlinequizzapp.dto.quiz.QuizResponseDto;
import org.varun.onlinequizzapp.dto.quiz.UpdateQuizDto;
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
        List<Quiz> quizzes = quizRepo.findAll();
        List<QuizResponseDto> responses = quizzes.stream().map(quiz -> new QuizResponseDto(quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTopic().getName(),
                quiz.getTimeLimitMinutes(),
                quiz.getDifficultyLevel(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt())).toList();
        log.info("[Get-Quizzes] All quizzes fetched successfully");
        return new ResponseEntity<>(new ApiResponse<>("All quizzes fetched successfully", responses), HttpStatus.OK);
    }

    public ResponseEntity<?> addQuiz(@Valid AddQuizDto input) {
        Topic topic = topicRepo.findById(input.topicId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic with id: " + input.topicId() + " not found"));
        if (quizRepo.existsQuizByTitleIgnoreCase(input.title().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quiz with the title already exists");
        }
        Quiz newQuiz = Quiz.builder()
                .title(input.title().trim())
                .description(input.description().trim())
                .topic(topic)
                .timeLimitMinutes(input.timeLimit())
                .difficultyLevel(input.difficulty())
                .build();
        quizRepo.saveAndFlush(newQuiz);
        log.info("[Add-Quiz] Quiz created successfully");
        return new ResponseEntity<>(new ApiResponse<>("Quiz created successfully"), HttpStatus.CREATED);

    }

    public ResponseEntity<?> deleteQuiz(Long id) {
        Quiz quiz=quizRepo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Quiz with id: "+id+" not found"));
        quizRepo.delete(quiz);
        log.info("[Delete-Quiz] Quiz with id {}, deleted successfully",id);
        return new ResponseEntity<>(new ApiResponse<>("Quiz with id: "+id+" deleted successfully"),HttpStatus.OK);
    }

    public ResponseEntity<?> updateQuiz(Long id, @Valid UpdateQuizDto input) {
        Quiz quiz=quizRepo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Quiz with id: "+id+" not found"));
        if(input.title()!=null && !input.description().isEmpty()){
            quiz.setTitle(input.title().trim());
        }
        if(input.description()!=null && !input.description().isEmpty()){
            quiz.setDescription(input.description().trim());
        }
        if(input.topicId()!=null){
            Topic topic=topicRepo.findById(input.topicId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Topic with id: "+input.topicId()+" not found"));
            quiz.setTopic(topic);
        }
        if(input.timeLimit()!=null){
            quiz.setTimeLimitMinutes(input.timeLimit());
        }
        if(input.difficulty()!=null){
            quiz.setDifficultyLevel(input.difficulty());
        }
        quizRepo.save(quiz);
        log.info("[Update-Quiz] Quiz with id {}, updated successfully",id);
        return new ResponseEntity<>(new ApiResponse<>("Quiz has been Updated"),HttpStatus.OK);
    }

    public ResponseEntity<?> getQuizWithId(Long id) {
        Quiz quiz=quizRepo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Quiz not found"));
        QuizResponseDto response=new QuizResponseDto(quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTopic().getName(),
                quiz.getTimeLimitMinutes(),
                quiz.getDifficultyLevel(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt());
        log.info("[Get-quiz] Quiz with id {}, fetched successfully",id);
        return new ResponseEntity<>(new ApiResponse<>("Quiz fetched successfully",response),HttpStatus.OK);
    }
}
