package com.pborsa.api.service.credentials;

/**
 * Interface for checking if a user has active strategies.
 * Used to prevent credential changes when strategies are running.
 */
public interface ActiveStrategyChecker {

    /**
     * Checks if the user has any active strategies (ACTIVE or PREPARING status).
     *
     * @param userId User ID
     * @return true if the user has active strategies
     */
    boolean hasActiveStrategies(Long userId);
}
