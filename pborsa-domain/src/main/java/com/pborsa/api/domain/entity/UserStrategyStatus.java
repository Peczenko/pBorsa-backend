package com.pborsa.api.domain.entity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Status of a user's strategy subscription.
 */
public enum UserStrategyStatus {
    /**
     * Strategy has been created but not yet activated.
     */
    CREATED,

    /**
     * Strategy is being prepared - data transfer to trading engine in progress.
     */
    PREPARING,

    /**
     * Strategy is actively running and generating orders.
     */
    ACTIVE,

    /**
     * Strategy is temporarily paused, can be resumed.
     */
    PAUSED,

    /**
     * Strategy has been stopped and will not generate new orders.
     */
    STOPPED,

    /**
     * Strategy failed to start - data transfer or preparation failed.
     * Can be retried by transitioning back to PREPARING.
     */
    START_FAILED;

    /**
     * Map of allowed transitions from each status.
     */
    public static final Map<UserStrategyStatus, Set<UserStrategyStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<UserStrategyStatus, Set<UserStrategyStatus>> transitions = new EnumMap<>(UserStrategyStatus.class);
        transitions.put(CREATED, EnumSet.of(PREPARING));
        transitions.put(PREPARING, EnumSet.of(ACTIVE, STOPPED, START_FAILED));
        transitions.put(ACTIVE, EnumSet.of(PAUSED, STOPPED));
        transitions.put(PAUSED, EnumSet.of(ACTIVE, STOPPED));
        transitions.put(STOPPED, EnumSet.noneOf(UserStrategyStatus.class));
        transitions.put(START_FAILED, EnumSet.of(PREPARING));
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    /**
     * Gets the set of statuses that can be transitioned to from this status.
     *
     * @return Unmodifiable set of allowed target statuses
     */
    public Set<UserStrategyStatus> getAllowedTransitions() {
        return ALLOWED_TRANSITIONS.get(this);
    }

    /**
     * Checks if transition to the target status is allowed.
     *
     * @param target Target status
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(UserStrategyStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }
}


