package org.varun.onlinequizzapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.varun.onlinequizzapp.dto.topic.AddTopicDto;
import org.varun.onlinequizzapp.dto.topic.UpdateTopicDto;
import org.varun.onlinequizzapp.service.TopicService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/topic")
public class TopicController {
    private final TopicService topicService;

    @GetMapping("topics")
    public ResponseEntity<?> getTopics() {
        return topicService.getTopics();
    }

    @GetMapping("get/{id}")
    public ResponseEntity<?> getTopicWithId(@PathVariable Long id){
        return topicService.getTopicWithId(id);
    }

    @PostMapping("add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addTopic(@Valid @RequestBody AddTopicDto input) {
        return topicService.addTopic(input);
    }

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteTopic(@PathVariable Long id, @RequestParam(required = false, defaultValue = "false") Boolean force) {
        return topicService.deleteTopic(id, force);
    }

    @PatchMapping("update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateTopic(@PathVariable Long id, @Valid @RequestBody UpdateTopicDto input) {
        return topicService.updateTopic(id, input);
    }
}
