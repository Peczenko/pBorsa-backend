package com.pborsa.domain.exception;

import lombok.Getter;

/**
 * Base exception for Alpaca API related errors.
 */
@Getter
public class AlpacaException extends RuntimeException {

    private final ErrorCode errorCode;

    public AlpacaException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public AlpacaException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public AlpacaException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AlpacaException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Error codes for Alpaca-related exceptions.
     */
    public enum ErrorCode {
        GENERAL_ERROR,
        API_ERROR,
        AUTHENTICATION_FAILED,
        INVALID_CREDENTIALS,
        CREDENTIALS_NOT_FOUND,
        RATE_LIMIT_EXCEEDED,
        INSUFFICIENT_FUNDS,
        INVALID_ORDER,
        ORDER_FAILED,
        ORDER_NOT_FOUND,
        POSITION_NOT_FOUND,
        SYMBOL_NOT_FOUND,
        MARKET_DATA_ERROR,
        MARKET_CLOSED,
        CONNECTION_ERROR,
        STREAMING_ERROR,
        TIMEOUT_ERROR,
        INVALID_SYMBOL,
        DATA_NOT_AVAILABLE
    }
}
