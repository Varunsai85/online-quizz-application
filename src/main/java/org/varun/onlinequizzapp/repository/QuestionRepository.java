package org.varun.onlinequizzapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.Question;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    boolean existsQuestionByTitleIgnoreCase(String title);

    Optional<Question> findByTitleIgnoreCase(String title);
}
