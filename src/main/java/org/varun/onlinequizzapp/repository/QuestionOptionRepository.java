package org.varun.onlinequizzapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.QuestionOption;

import java.util.Optional;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    Optional<QuestionOption> findByOptionTextIgnoreCase(String optionText);
}
