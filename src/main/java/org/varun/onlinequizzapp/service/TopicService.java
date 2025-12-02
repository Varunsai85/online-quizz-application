package org.varun.onlinequizzapp.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.varun.onlinequizzapp.dto.AddTopicDto;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.TopicResponseDto;
import org.varun.onlinequizzapp.model.Topic;
import org.varun.onlinequizzapp.repository.TopicRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {
    private final TopicRepository topicRepo;

    public ResponseEntity<?> getTopics() {
        List<Topic> topics = topicRepo.findAll();
        List<TopicResponseDto> responses = topics.stream().map(topic -> new TopicResponseDto(topic.getId(), topic.getName(), topic.getDescription())).toList();
        return new ResponseEntity<>(new ApiResponse<>("All topics fetched", responses), HttpStatus.OK);
    }

    public ResponseEntity<?> addTopic(@Valid AddTopicDto input) {
        try {
            if (topicRepo.existsTopicsByNameIgnoreCase(input.name())) {
                return new ResponseEntity<>(new ApiResponse<>("Topic with the name already exists"), HttpStatus.CONFLICT);
            }
            Topic newTopic = Topic.builder()
                    .name(input.name())
                    .description(input.description())
                    .build();
            topicRepo.save(newTopic);
            return new ResponseEntity<>(new ApiResponse<>("Topic created successfully"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("[Add-Topic] Error adding topic in addTopic method");
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
