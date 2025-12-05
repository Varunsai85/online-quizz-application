package org.varun.onlinequizzapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.service.UserService;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("getUsers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            return userService.getAllUsers();
        }catch (Exception e){
            return new ResponseEntity<>(new ApiResponse<>(false,"Needs admin privileges",e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }
}
