package org.varun.onlinequizzapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.QuizAttempt;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
}
