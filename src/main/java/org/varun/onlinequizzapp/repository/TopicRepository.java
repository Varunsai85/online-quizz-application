package org.varun.onlinequizzapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
