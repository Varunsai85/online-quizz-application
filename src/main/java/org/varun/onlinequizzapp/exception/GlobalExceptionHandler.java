package org.varun.onlinequizzapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.varun.onlinequizzapp.dto.ApiResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ":" + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation Failed");

        return new ResponseEntity<>(new ApiResponse<>(errMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
        return new ResponseEntity<>(new ApiResponse<>(ex.getMessage()), ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, HttpServletRequest req) {
        log.error("[{}] {} | Exception: {} | Message: {}", req.getMethod(), req.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage());
        return new ResponseEntity<>(new ApiResponse<>("Something went wrong", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
