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
    private final QuestionOptionRepository optionRepo;

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

    @Transactional
    public ResponseEntity<?> addQuestion(@Valid AddQuestionDto input) {
        Quiz quiz = quizRepo.findById(input.quizId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz with id: " + input.quizId() + " not found"));
        if (questionRepo.existsQuestionByTitleIgnoreCase(input.title().trim())) {
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
        log.info("[Update-Question] Question with id {}, updated successfully", id);
        return new ResponseEntity<>(new ApiResponse<>("Question Updated successfully"), HttpStatus.OK);
    }

    public ResponseEntity<?> getQuestionWithId(Long id) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        QuestionResponseDto response = new QuestionResponseDto(
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
        log.info("[Get-Question] Quiz with id {}, fetched successfully", id);
        return new ResponseEntity<>(new ApiResponse<>("Question fetched successfully", response), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteQuestion(Long id) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question with id: " + id + " not found"));
        questionRepo.delete(question);
        log.info("[Delete-Question] Question with id {}, deleted successfully", id);
        return new ResponseEntity<>(new ApiResponse<>("Question deleted successfully"), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> addOption(Long id, @Valid AddOptionDto input) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question with id: " + id + " not found"));
        if (input.isCorrect()) {
            int count = (int) question.getQuestionOptions().stream().filter(QuestionOption::getIsCorrect).count();
            if (count == 1) {
                log.warn("[Add-Option] Question with id {}, already has a correct option", id);
                return new ResponseEntity<>(new ApiResponse<>("Question already has a correct option"), HttpStatus.CONFLICT);
            }
        }
        Optional<QuestionOption> existingOption = optionRepo.findByOptionTextIgnoreCase(input.optionText().trim());
        if (existingOption.isPresent() && existingOption.get().getQuestion().getId().equals(id)) {
            log.warn("[Add-Option] Option already exists in the question with id {}", id);
            return new ResponseEntity<>(new ApiResponse<>("Option already exist"), HttpStatus.CONFLICT);
        }
        QuestionOption newOption = QuestionOption.builder()
                .optionText(input.optionText().trim())
                .isCorrect(input.isCorrect())
                .question(question)
                .build();
        optionRepo.save(newOption);
        log.info("[Add-Option] Option added to the question with id {}", id);
        return new ResponseEntity<>(new ApiResponse<>("Option created successfully"), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> updateOption(Long id, @Valid UpdateOptionDto input) {
        QuestionOption option = optionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option with id: " + id + " not found"));
        Optional<QuestionOption> existingOption = optionRepo.findByOptionTextIgnoreCase(input.optionText().trim());

        if (existingOption.isPresent() && !existingOption.get().getId().equals(option.getId()) && existingOption.get().getQuestion().getId().equals(option.getQuestion().getId())) {
            log.warn("[Update-Option] Option with id {}, already exists with the name", id);
            return new ResponseEntity<>(new ApiResponse<>("Option already exists with the text"), HttpStatus.CONFLICT);
        }

        if (option.getIsCorrect() && !input.isCorrect()) {
            int correctOptionsCount = (int) option.getQuestion().getQuestionOptions().stream().filter(QuestionOption::getIsCorrect).count();
            if (correctOptionsCount <= 1) {
                log.warn("[Update-Option] Option with id {}, cannot be updated as incorrect, must choose another option as correct to make this incorrect", id);
                return new ResponseEntity<>(new ApiResponse<>("Cannot mark this option as incorrect. At least ot option must be marked correct"), HttpStatus.BAD_REQUEST);
            }
        }
        option.setOptionText(input.optionText().trim());
        option.setIsCorrect(input.isCorrect());
        log.info("[Update-Option] Option with id {}, updated successfully", id);
        return new ResponseEntity<>(new ApiResponse<>("Option Updated Successfully"), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteOption(Long id) {
        QuestionOption option = optionRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option with id: " + id + " not found"));

        if (option.getIsCorrect()) {
            int correctOptionCount = (int) option.getQuestion().getQuestionOptions().stream().filter(QuestionOption::getIsCorrect).count();
            if (correctOptionCount <= 1) {
                log.warn("[Delete-Option] Option with id {}, cannot delete the option because it is only correct option", id);
                return new ResponseEntity<>(new ApiResponse<>("Cannot delete this option because it it the only correct option"), HttpStatus.BAD_REQUEST);
            }
        }
        optionRepo.delete(option);
        log.info("[Delete-Option] Option with id {}, deleted successfully", id);
        return new ResponseEntity<>(new ApiResponse<>("Option deleted successfully"), HttpStatus.OK);
    }
}
