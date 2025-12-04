package org.varun.onlinequizzapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.varun.onlinequizzapp.dto.option.AddOptionDto;
import org.varun.onlinequizzapp.dto.option.UpdateOptionDto;
import org.varun.onlinequizzapp.service.QuestionService;

@RestController
@RequestMapping("api/option")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class OptionController {
    private final QuestionService questionService;

    @PostMapping("add/{questionId}")
    public ResponseEntity<?> addOption(@PathVariable Long questionId, @Valid @RequestBody AddOptionDto input) {
        return questionService.addOption(questionId, input);
    }

    @PatchMapping("update/{id}")
    public ResponseEntity<?> updateOption(@PathVariable Long id, @Valid @RequestBody UpdateOptionDto input) {
        return questionService.updateOption(id, input);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteOption(@PathVariable Long id) {
        return questionService.deleteOption(id);
    }
}
