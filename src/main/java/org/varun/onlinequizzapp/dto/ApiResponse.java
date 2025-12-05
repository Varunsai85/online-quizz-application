package org.varun.onlinequizzapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiResponse<T> {
    private T data;
    private String message;
    private Boolean success;

    public ApiResponse(Boolean success,String message, T data) {
        this.success=success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(Boolean success,String message) {
        this.success=success;
        this.message = message;
    }
}
