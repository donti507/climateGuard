package com.climateguard.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // Only present for validation failures (400s from MethodArgumentNotValidException).
    // Omitted from the JSON body entirely for every other error type.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> fieldErrors;

    // Convenience constructor for the non-validation error handlers, which
    // don't have field-level errors to report.
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
}
