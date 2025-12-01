package org.varun.onlinequizzapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
