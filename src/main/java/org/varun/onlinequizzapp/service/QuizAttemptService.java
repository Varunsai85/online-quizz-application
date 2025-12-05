package org.varun.onlinequizzapp.service;

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
import org.varun.onlinequizzapp.dto.quizAttempt.QuizAttemptResponseDto;
import org.varun.onlinequizzapp.dto.userAnswers.UserAnswersResponseDto;
import org.varun.onlinequizzapp.model.QuizAttempt;
import org.varun.onlinequizzapp.model.User;
import org.varun.onlinequizzapp.repository.QuizRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptService {
    private final QuizRepository quizRepo;

    @Transactional
    public ResponseEntity<?> getAttemptsFromQuizAndUserId(Long quizId) {
        User user = getCurrentUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        quizRepo.findById(quizId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        List<QuizAttempt> attempts = user.getQuizAttempts();
        List<QuizAttemptResponseDto> responses = attempts.stream().filter(attempt -> attempt.getQuiz().getId().equals(quizId)).map(this::mapToResponseDto).toList();
        log.info("[Get-Attempts] Quiz attempts associated with quiz id {}, fetched successfully", quizId);
        return new ResponseEntity<>(new ApiResponse<>(true, "Quiz attempts fetched successfully", responses), HttpStatus.OK);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
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
