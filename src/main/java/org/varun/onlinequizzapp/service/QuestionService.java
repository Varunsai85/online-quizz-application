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
import org.varun.onlinequizzapp.dto.option.UpdateOptionDto;
import org.varun.onlinequizzapp.dto.question.AddQuestionDto;
import org.varun.onlinequizzapp.dto.option.OptionResponseDto;
import org.varun.onlinequizzapp.dto.question.QuestionResponseDto;
import org.varun.onlinequizzapp.dto.question.UpdateQuestionDto;
import org.varun.onlinequizzapp.model.Question;
import org.varun.onlinequizzapp.model.QuestionOption;
import org.varun.onlinequizzapp.model.Quiz;
import org.varun.onlinequizzapp.repository.QuestionOptionRepository;
import org.varun.onlinequizzapp.repository.QuestionRepository;
import org.varun.onlinequizzapp.repository.QuizRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepo;
    private final QuizRepository quizRepo;
    private final QuestionOptionRepository optionRepo;

    @Transactional
    public ResponseEntity<?> getAllQuestions() {
        List<Question> questions = questionRepo.findAll();
        List<QuestionResponseDto> responses = questions.stream().map(this::mapToResponseDto).toList();
        log.info("[Get-Questions] All questions fetched successfully");
        return new ResponseEntity<>(new ApiResponse<>(true, "All questions fetched successfully", responses), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> addQuestion(Long id, @Valid AddQuestionDto input) {
        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz with id: " + id + " not found"));
        Optional<Question> existingQuestion = questionRepo.findByTitleIgnoreCase(input.title().trim());
        if (existingQuestion.isPresent() && existingQuestion.get().getQuiz().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Question with the title already exists in the quiz");
        }

        Question newQuestion = Question.builder()
                .title(input.title().trim())
                .quiz(quiz)
                .build();

        questionRepo.save(newQuestion);
        log.info("[Add-Question] Successfully added question to quiz with id {}", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Question Added successfully"), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> updateQuestion(Long id, @Valid UpdateQuestionDto input) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question with id: " + id + ", not found"));
        String newTitle = input.title().trim();
        Optional<Question> existingQuestion = questionRepo.findByTitleIgnoreCase(newTitle);
        if (existingQuestion.isPresent() && !existingQuestion.get().getId().equals(question.getId()) && existingQuestion.get().getQuiz().getId().equals(question.getQuiz().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Question already exists in the quiz");
        }

        question.setTitle(newTitle);
        questionRepo.save(question);
        log.info("[Update-Question] Question with id {}, updated successfully", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Question Updated successfully"), HttpStatus.OK);
    }

    public ResponseEntity<?> getQuestionWithId(Long id) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        QuestionResponseDto response = mapToResponseDto(question);
        log.info("[Get-Question] Quiz with id {}, fetched successfully", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Question fetched successfully", response), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteQuestion(Long id) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question with id: " + id + " not found"));
        questionRepo.delete(question);
        log.info("[Delete-Question] Question with id {}, deleted successfully", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Question deleted successfully"), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> addOption(Long id, @Valid AddOptionDto input) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question with id: " + id + " not found"));
        if (input.isCorrect()) {
            int count = (int) question.getQuestionOptions().stream().filter(QuestionOption::getIsCorrect).count();
            if (count == 1) {
                log.warn("[Add-Option] Question with id {}, already has a correct option", id);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Question already has a correct option");
            }
        }
        Optional<QuestionOption> existingOption = optionRepo.findByOptionTextIgnoreCase(input.optionText().trim());
        if (existingOption.isPresent() && existingOption.get().getQuestion().getId().equals(id)) {
            log.warn("[Add-Option] Option already exists in the question with id {}", id);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Option already exist");
        }
        QuestionOption newOption = QuestionOption.builder()
                .optionText(input.optionText().trim())
                .isCorrect(input.isCorrect())
                .question(question)
                .build();
        optionRepo.save(newOption);
        log.info("[Add-Option] Option added to the question with id {}", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Option created successfully"), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> updateOption(Long id, @Valid UpdateOptionDto input) {
        QuestionOption option = optionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option with id: " + id + " not found"));
        Optional<QuestionOption> existingOption = optionRepo.findByOptionTextIgnoreCase(input.optionText().trim());

        if (existingOption.isPresent() && !existingOption.get().getId().equals(option.getId()) && existingOption.get().getQuestion().getId().equals(option.getQuestion().getId())) {
            log.warn("[Update-Option] Option with id {}, already exists with the name", id);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Option already exists with the text");
        }

        if (option.getIsCorrect() && !input.isCorrect()) {
            int correctOptionsCount = (int) option.getQuestion().getQuestionOptions().stream().filter(QuestionOption::getIsCorrect).count();
            if (correctOptionsCount <= 1) {
                log.warn("[Update-Option] Option with id {}, cannot be updated as incorrect, must choose another option as correct to make this incorrect", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot mark this option as incorrect. At least ot option must be marked correct");
            }
        }
        option.setOptionText(input.optionText().trim());
        option.setIsCorrect(input.isCorrect());
        log.info("[Update-Option] Option with id {}, updated successfully", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Option Updated Successfully"), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteOption(Long id) {
        QuestionOption option = optionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option with id: " + id + " not found"));

        if (option.getIsCorrect()) {
            int correctOptionCount = (int) option.getQuestion().getQuestionOptions().stream().filter(QuestionOption::getIsCorrect).count();
            if (correctOptionCount <= 1) {
                log.warn("[Delete-Option] Option with id {}, cannot delete the option because it is only correct option", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete this option because it it the only correct option");
            }
        }
        optionRepo.delete(option);
        log.info("[Delete-Option] Option with id {}, deleted successfully", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Option deleted successfully"), HttpStatus.OK);
    }

    private QuestionResponseDto mapToResponseDto(Question question) {
        return new QuestionResponseDto(
                question.getId(),
                question.getTitle(),
                question.getQuiz().getId(),
                question.getQuestionOptions()
                        .stream()
                        .map(option -> new OptionResponseDto(option.getId(),
                                option.getOptionText(),
                                option.getIsCorrect())).toList()
        );
    }
}