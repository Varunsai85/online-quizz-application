package org.varun.onlinequizzapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.varun.onlinequizzapp.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex){
        String errMessage=ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error-> error.getField()+":"+error.getDefaultMessage())
                .findFirst()
                .orElse("Validation Failed");

        return new ResponseEntity<>(new ApiResponse<>(errMessage), HttpStatus.BAD_REQUEST);
    }
}
