package org.varun.onlinequizzapp.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.quizAttempt.AnswerFeedbackDto;
import org.varun.onlinequizzapp.dto.quizAttempt.QuizAttemptResponseDto;
import org.varun.onlinequizzapp.dto.quizAttempt.SubmitAnswerDto;
import org.varun.onlinequizzapp.dto.userAnswers.UserAnswersResponseDto;
import org.varun.onlinequizzapp.model.*;
import org.varun.onlinequizzapp.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptService {
    private final QuizRepository quizRepo;
    private final QuizAttemptRepository attemptRepo;
    private final UserRepository userRepo;
    private final QuestionRepository questionRepo;
    private final QuestionOptionRepository optionRepo;
    private final UserAnswerRepository userAnswerRepo;

    @Transactional
    public ResponseEntity<?> getAttemptsFromQuizAndUserId(Long quizId) {
        User user = getCurrentUser();
        quizRepo.findById(quizId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        List<QuizAttempt> attempts = user.getQuizAttempts();
        List<QuizAttemptResponseDto> responses = attempts.stream().filter(attempt -> attempt.getQuiz().getId().equals(quizId)).map(this::mapToResponseDto).toList();
        log.info("[Get-Attempts] Quiz attempts associated with quiz id {}, fetched successfully", quizId);
        return new ResponseEntity<>(new ApiResponse<>(true, "Quiz attempts fetched successfully", responses), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> addAttempt(Long quizId) {
        User user = getCurrentUser();
        Quiz quiz = quizRepo.findById(quizId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        List<QuizAttempt> userAttempts = user.getQuizAttempts().stream().filter(attempt -> attempt.getQuiz().getId().equals(quizId)).toList();
        boolean incompleteAttempt = userAttempts.stream().anyMatch(attempt -> !attempt.getIsCompleted());
        if (incompleteAttempt) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ypu have an incomplete attempt for the quiz");
        }

        QuizAttempt newAttempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .score(0)
                .totalQuestions(quiz.getQuestions().size())
                .attemptedQuestions(0)
                .startedAt(LocalDateTime.now())
                .isCompleted(false)
                .build();
        attemptRepo.save(newAttempt);
        log.info("[Add-QuizAttempt] Successfully created attempt for quizId {} with userId {}", quiz.getId(), user.getId());
        return new ResponseEntity<>(new ApiResponse<>(true, "Successfully created the quiz attempt"), HttpStatus.CREATED);
    }

    public ResponseEntity<?> submitAnswer(Long attemptId, @Valid SubmitAnswerDto input) {
        User user = getCurrentUser();
        QuizAttempt attempt = attemptRepo.findById(attemptId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found"));

        if (!attempt.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access to quiz attempt");
        if (attempt.getIsCompleted())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz already completed");

        Question question = questionRepo.findById(input.questionId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question nor found"));
        if (!question.getQuiz().getId().equals(attempt.getQuiz().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question doesn't belong to the current quiz");

        QuestionOption option = optionRepo.findById(input.selectedOptionId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected option not found"));

        if (!option.getQuestion().getId().equals(question.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected option doesn't belong to the question");

        Optional<UserAnswer> existingAnswer = userAnswerRepo.findByQuizAttemptIdAndQuestionId(attemptId, input.questionId());

        UserAnswer userAnswer;
        if (existingAnswer.isPresent()) {
            userAnswer = existingAnswer.get();

            if (userAnswer.getIsCorrect() && !option.getIsCorrect()) {
                attempt.setScore(attempt.getScore() - 1);
            } else if (!userAnswer.getIsCorrect() && option.getIsCorrect()) {
                attempt.setScore(attempt.getScore() + 1);
            }

            userAnswer.setSelectedOption(option);
            userAnswer.setIsCorrect(option.getIsCorrect());
            userAnswer.setAnsweredAt(LocalDateTime.now());
        } else {
            userAnswer = UserAnswer.builder()
                    .quizAttempt(attempt)
                    .question(question)
                    .selectedOption(option)
                    .isCorrect(option.getIsCorrect())
                    .answeredAt(LocalDateTime.now())
                    .build();

            attempt.setAttemptedQuestions(attempt.getAttemptedQuestions() + 1);
            if (option.getIsCorrect()) {
                attempt.setScore(attempt.getScore() + 1);
            }
        }
        UserAnswer savedAnswer = userAnswerRepo.save(userAnswer);
        attemptRepo.save(attempt);

        QuestionOption correctOption = question.getQuestionOptions().stream()
                .filter(QuestionOption::getIsCorrect)
                .findFirst()
                .orElse(null);

        assert correctOption != null;
        AnswerFeedbackDto feedback = new AnswerFeedbackDto(
                question.getId(), question.getTitle(),option.getId(),option.getOptionText(),savedAnswer.getIsCorrect(),correctOption.getId(),correctOption.getOptionText()
        );

        log.info("[Submit-Answer] User {} submitted answer for question {} in attempt {}",
                user.getId(), question.getId(), attemptId);

        return new ResponseEntity<>(
                new ApiResponse<>(true, "Answer submitted successfully", feedback),
                HttpStatus.OK
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User currentUser) {
            return userRepo.findById(currentUser.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    private QuizAttemptResponseDto mapToResponseDto(QuizAttempt attempt) {
        return new QuizAttemptResponseDto(
                attempt.getId(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                attempt.getIsCompleted(),
                attempt.getUserAnswers().stream().map(answer -> new UserAnswersResponseDto(answer.getId(),
                        answer.getIsCorrect(),
                        answer.getAnsweredAt())).toList());
    }
}
