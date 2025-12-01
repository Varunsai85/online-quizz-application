package org.varun.onlinequizzapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
