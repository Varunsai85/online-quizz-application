package org.varun.onlinequizzapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.varun.onlinequizzapp.dto.AddTopicDto;
import org.varun.onlinequizzapp.service.TopicService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api")
public class TopicController {
    private final TopicService topicService;

    @GetMapping("topics")
    public ResponseEntity<?> getTopics() {
        return topicService.getTopics();
    }

    @PostMapping("topic")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addTopic(@Valid @RequestBody AddTopicDto input) {
        return topicService.addTopic(input);
    }

    @DeleteMapping("topic/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteTopic(@PathVariable Long id, @RequestParam(required = false, defaultValue = "false") Boolean force) {
        return topicService.deleteTopic(id, force);
    }
}
