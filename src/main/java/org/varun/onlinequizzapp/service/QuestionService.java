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
import org.varun.onlinequizzapp.dto.option.AddOptionDto;
import org.varun.onlinequizzapp.dto.question.AddQuestionDto;
import org.varun.onlinequizzapp.dto.option.OptionResponseDto;
import org.varun.onlinequizzapp.dto.question.QuestionResponseDto;
import org.varun.onlinequizzapp.dto.question.UpdateQuestionDto;
import org.varun.onlinequizzapp.model.Question;
import org.varun.onlinequizzapp.model.QuestionOption;
import org.varun.onlinequizzapp.model.Quiz;
import org.varun.onlinequizzapp.repository.QuestionRepository;
import org.varun.onlinequizzapp.repository.QuizRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepo;
    private final QuizRepository quizRepo;

    @Transactional
    public ResponseEntity<?> getAllQuestions() {
        List<Question> questions = questionRepo.findAll();
        List<QuestionResponseDto> responses = questions.stream().map(question -> new QuestionResponseDto(question.getId(),
                        question.getTitle(),
                        question.getQuiz().getId(),
                        question.getOrderNumber(),
                        question.getQuestionOptions()
                                .stream()
                                .map(option -> new OptionResponseDto(option.getId(),
                                        option.getOptionText(),
                                        option.getIsCorrect()))
                                .toList())).toList();
        return new ResponseEntity<>(new ApiResponse<>("All questions fetched successfully", responses), HttpStatus.OK);
    }

    public ResponseEntity<?> addQuestion(@Valid AddQuestionDto input) {
        Quiz quiz = quizRepo.findById(input.quizId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz with id: " + input.quizId() + " not found"));
        if (questionRepo.existsQuestionByTitleIgnoreCase(input.title())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quiz with the title already exists");
        }

        boolean hasCorrectAnswer = input.options().stream().anyMatch(AddOptionDto::isCorrect);

        Set<String> uniqueOptions = new HashSet<>();
        for (AddOptionDto option : input.options()) {
            String normalizedOption = option.optionText().trim();
            if (!uniqueOptions.add(normalizedOption)) {
                return new ResponseEntity<>(new ApiResponse<>("Duplicate Option Found", option.optionText()), HttpStatus.CONFLICT);
            }
        }

        if (!hasCorrectAnswer) {
            return new ResponseEntity<>(new ApiResponse<>("At least one option must be marked as correct"), HttpStatus.BAD_REQUEST);
        }

        Question newQuestion = Question.builder()
                .title(input.title().trim())
                .quiz(quiz)
                .orderNumber(input.order())
                .build();

        List<QuestionOption> options = input.options().stream()
                .map(option -> QuestionOption.builder()
                        .optionText(option.optionText())
                        .isCorrect(option.isCorrect())
                        .question(newQuestion)
                        .build())
                .toList();
        newQuestion.setQuestionOptions(options);

        questionRepo.save(newQuestion);
        return new ResponseEntity<>(new ApiResponse<>("Question Added successfully"), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> updateQuestion(Long id, @Valid UpdateQuestionDto input) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question with id: " + id + ", not found"));
        String newTitle = input.title().trim();
        Optional<Question> existingQuestion = questionRepo.findByTitleIgnoreCase(newTitle);
        if (existingQuestion.isPresent() && !existingQuestion.get().getId().equals(question.getId()) && existingQuestion.get().getQuiz().getId().equals(question.getQuiz().getId())) {
            return new ResponseEntity<>(new ApiResponse<>("Question already exists in the quiz"), HttpStatus.CONFLICT);
        }

        question.setTitle(newTitle);
        question.setOrderNumber(input.order());
        questionRepo.save(question);
        log.info("[Update-Question] Question with id {}, updated successfully",id);
        return new ResponseEntity<>(new ApiResponse<>("Question Updated successfully"),HttpStatus.OK);
    }

    public ResponseEntity<?> getQuestionWithId(Long id) {
        Question question=questionRepo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Question not found"));
        QuestionResponseDto response =new QuestionResponseDto(
                question.getId(),
                question.getTitle(),
                question.getQuiz().getId(),
                question.getOrderNumber(),
                question.getQuestionOptions().stream().map(option -> new OptionResponseDto(
                        option.getId(),
                        option.getOptionText(),
                        option.getIsCorrect()
                )).toList()
        );
        log.info("[Get-Question] Quiz with id {}, fetched successfully",id);
        return new ResponseEntity<>(new ApiResponse<>("Question fetched successfully",response),HttpStatus.OK);
    }
}
