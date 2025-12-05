package org.varun.onlinequizzapp.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.varun.onlinequizzapp.dto.topic.AddTopicDto;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.topic.TopicResponseDto;
import org.varun.onlinequizzapp.dto.topic.UpdateTopicDto;
import org.varun.onlinequizzapp.model.Topic;
import org.varun.onlinequizzapp.repository.TopicRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepo;

    public ResponseEntity<?> getTopics() {
        List<Topic> topics = topicRepo.findAll();
        List<TopicResponseDto> responses = topics.stream().map(this::mapToResponseDto).toList();
        log.info("[Get-Topics] All topics fetched successfully");
        return new ResponseEntity<>(new ApiResponse<>(true, "All topics fetched", responses), HttpStatus.OK);
    }

    public ResponseEntity<?> addTopic(@Valid AddTopicDto input) {
        if (topicRepo.existsTopicsByNameIgnoreCase(input.name().trim())) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Topic with the name already exists"), HttpStatus.CONFLICT);
        }
        Topic newTopic = Topic.builder()
                .name(input.name().trim())
                .description(input.description().trim())
                .build();
        topicRepo.save(newTopic);
        log.info("[Add-Topic] New Topic has been created successfully");
        return new ResponseEntity<>(new ApiResponse<>(true, "Topic created successfully"), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> deleteTopic(Long id, Boolean force) {
        Topic topic = topicRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic with id: " + id + " not found"));

        if (!topic.getQuizzes().isEmpty() && !force) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Topic has " + topic.getQuizzes().size() + " quizzes associated.");
        }

        topicRepo.deleteById(id);
        String message = force ? "Topic and associated quizzes are deleted" : "Topic deleted successfully";
        log.info("[Delete-Topic] Topic with id {}, deleted successfully", id);
        return new ResponseEntity<>(new ApiResponse<>(true, message), HttpStatus.OK);
    }

    public ResponseEntity<?> updateTopic(Long id, @Valid UpdateTopicDto input) {
        Topic topic = topicRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic with id: " + id + " not found"));

        if (input.name() == null && input.description() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nothing to update");
        }

        if (input.name() != null && !input.name().trim().isEmpty()) {
            String newName = input.name().trim();
            Optional<Topic> existingName = topicRepo.findByName(newName);
            if (existingName.isPresent() && !existingName.get().getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Topic with this name already exists");
            }
            topic.setName(newName);
        }

        if (input.description() != null) {
            topic.setDescription(input.description().trim());
        }

        Topic updatedTopic = topicRepo.save(topic);
        log.info("[Update_Topic] Topic with id {}, updated successfully", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Topic updated successfully", updatedTopic), HttpStatus.OK);
    }

    public ResponseEntity<?> getTopicWithId(Long id) {
        Topic topic = topicRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
        TopicResponseDto response = mapToResponseDto(topic);
        log.info("[Get-Topic] Topic with id {}, fetched successfully", id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Topic fetched successfully", response), HttpStatus.OK);
    }

    private TopicResponseDto mapToResponseDto(Topic topic) {
        return new TopicResponseDto(
                topic.getId(),
                topic.getName(),
                topic.getDescription()
        );
    }
}
