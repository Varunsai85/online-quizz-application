package org.varun.onlinequizzapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.QuestionOption;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
}
