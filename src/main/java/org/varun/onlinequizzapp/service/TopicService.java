package org.varun.onlinequizzapp.service;

import jakarta.transaction.Transactional;
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
import java.util.Map;
import java.util.Optional;

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
            log.error("[Add-Topic] Error adding topic in addTopic method: {}",e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> deleteTopic(Long id, Boolean force) {
        try {
            Optional<Topic> topicOptional=topicRepo.findById(id);
            if(topicOptional.isEmpty()){
                return new ResponseEntity<>(new ApiResponse<>("Topic not found"),HttpStatus.NOT_FOUND);
            }
            Topic topic=topicOptional.get();
            if(!topic.getQuizzes().isEmpty() && !force){
                return new ResponseEntity<>(new ApiResponse<>("Topic has "+topic.getQuizzes().size()+" quizzes associated.", Map.of("quiz_size",topic.getQuizzes().size())),HttpStatus.CONFLICT);
            }

            topicRepo.deleteById(id);
            String message=force?"Topic and associated quizzes are deleted":"Topic deleted successfully";
            return new ResponseEntity<>(new ApiResponse<>(message),HttpStatus.OK);
        }catch (Exception e){
            log.error("[Delete-Topic] Error while deleting topic with id {}: {}",id,e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong",e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
