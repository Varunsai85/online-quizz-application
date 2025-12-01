package org.varun.onlinequizzapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.UserAnswer;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
}
