package com.pborsa.api.controller.exception;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.exception.CredentialsNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCredentialsNotFound(CredentialsNotFoundException ex) {
        log.warn("Credentials not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AlpacaException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlpacaException(AlpacaException ex) {
        log.error("Alpaca API error: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        HttpStatus status = switch (ex.getErrorCode()) {
            case AUTHENTICATION_FAILED, INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case CREDENTIALS_NOT_FOUND, ORDER_NOT_FOUND, POSITION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case INSUFFICIENT_FUNDS, INVALID_ORDER, INVALID_SYMBOL -> HttpStatus.BAD_REQUEST;
            case MARKET_CLOSED -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getErrorCode().name(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .data(errors)
                        .error("Validation failed")
                        .timestamp(java.time.Instant.now())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}

