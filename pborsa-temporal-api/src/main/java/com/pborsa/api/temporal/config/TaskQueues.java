package com.pborsa.api.temporal.config;

/**
 * Task queue constants for Temporal workflows and activities.
 * Centralized to ensure consistency across API and worker modules.
 */
public final class TaskQueues {
    
    public static final String TRADING_TASK_QUEUE = "TRADING_TASK_QUEUE";
    public static final String MARKET_DATA_TASK_QUEUE = "MARKET_DATA_TASK_QUEUE";
    public static final String STRATEGY_EXECUTION_TASK_QUEUE = "STRATEGY_EXECUTION_TASK_QUEUE";
    
    private TaskQueues() {
        // Utility class
    }
}

