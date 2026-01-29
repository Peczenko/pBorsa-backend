package com.pborsa.api.exception;

/**
 * Exception thrown when attempting to modify or delete credentials
 * while the user has active strategies running.
 */
public class ActiveStrategiesExistException extends RuntimeException {

    private final Long userId;

    public ActiveStrategiesExistException(Long userId) {
        super("Cannot modify credentials: user " + userId + " has active strategies. " +
                "Please stop all strategies before changing credentials.");
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
