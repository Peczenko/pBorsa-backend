package com.pborsa.api.domain.entity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Status of a backtest execution.
 */
public enum BacktestStatus {
    /**
     * Backtest has been created but not yet started.
     */
    CREATED,

    /**
     * Backtest is being prepared - fetching historical data.
     */
    PREPARING,

    /**
     * Backtest is running - executing strategy against historical data.
     */
    RUNNING,

    /**
     * Backtest has completed successfully.
     */
    COMPLETED,

    /**
     * Backtest has failed due to an error.
     */
    FAILED;

    /**
     * Map of allowed transitions from each status.
     */
    public static final Map<BacktestStatus, Set<BacktestStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<BacktestStatus, Set<BacktestStatus>> transitions = new EnumMap<>(BacktestStatus.class);
        transitions.put(CREATED, EnumSet.of(PREPARING));
        transitions.put(PREPARING, EnumSet.of(RUNNING, FAILED));
        transitions.put(RUNNING, EnumSet.of(COMPLETED, FAILED));
        transitions.put(COMPLETED, EnumSet.noneOf(BacktestStatus.class));
        transitions.put(FAILED, EnumSet.noneOf(BacktestStatus.class));
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    /**
     * Gets the set of statuses that can be transitioned to from this status.
     *
     * @return Unmodifiable set of allowed target statuses
     */
    public Set<BacktestStatus> getAllowedTransitions() {
        return ALLOWED_TRANSITIONS.get(this);
    }

    /**
     * Checks if transition to the target status is allowed.
     *
     * @param target Target status
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(BacktestStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }

    /**
     * Checks if this status is a terminal state.
     *
     * @return true if no further transitions are allowed
     */
    public boolean isTerminal() {
        return getAllowedTransitions().isEmpty();
    }
}
