package com.pborsa.api.controller.exception;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.exception.AccessDeniedException;
import com.pborsa.api.exception.AlpacaException;
import com.pborsa.api.exception.CredentialsNotFoundException;
import com.pborsa.api.exception.StrategyExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", ex.getMessage()));
    }

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
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = "Parameter '" + ex.getName() + "' must be of type " + requiredType;
        log.warn("Type mismatch: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_PARAMETER_TYPE", message));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "[]";
        String message = "Method " + ex.getMethod() + " not allowed. Supported: " + supported;
        log.warn("Method not supported: {}", message);
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("METHOD_NOT_ALLOWED", message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        String message = "No resource found for " + ex.getHttpMethod() + " " + ex.getResourcePath();
        log.warn("No resource found: {}", message);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(StrategyExecutionException.class)
    public ResponseEntity<ApiResponse<Void>> handleStrategyExecutionException(StrategyExecutionException ex) {
        log.error("Strategy execution error: {} - {}", ex.getErrorCode(), ex.getMessage());
        HttpStatus status = switch (ex.getErrorCode()) {
            case STRATEGY_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case TRADING_ENGINE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case CREDENTIALS_MISSING -> HttpStatus.UNAUTHORIZED;
            case DATA_STREAM_ERROR -> HttpStatus.BAD_GATEWAY;
            case INSUFFICIENT_FUNDS -> HttpStatus.FORBIDDEN;
        };
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getErrorCode().name(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}

