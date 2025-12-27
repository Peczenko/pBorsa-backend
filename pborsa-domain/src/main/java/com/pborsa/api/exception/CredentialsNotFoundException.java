package com.pborsa.api.exception;

/**
 * Exception thrown when user credentials are not found.
 */
public class CredentialsNotFoundException extends AlpacaException {

    public CredentialsNotFoundException(Long userId) {
        super(ErrorCode.CREDENTIALS_NOT_FOUND,
                "Alpaca credentials not found for user: " + userId);
    }
}

