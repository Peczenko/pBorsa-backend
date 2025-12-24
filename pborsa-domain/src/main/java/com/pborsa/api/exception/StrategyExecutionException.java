package com.pborsa.api.exception;

import lombok.Getter;

/**
 * Exception thrown during strategy execution orchestration or streaming.
 */
@Getter
public class StrategyExecutionException extends RuntimeException {

    private final ErrorCode errorCode;

    public StrategyExecutionException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public StrategyExecutionException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        STRATEGY_NOT_FOUND,
        INVALID_REQUEST,
        TRADING_ENGINE_UNAVAILABLE,
        DATA_STREAM_ERROR,
        CREDENTIALS_MISSING
    }
}
