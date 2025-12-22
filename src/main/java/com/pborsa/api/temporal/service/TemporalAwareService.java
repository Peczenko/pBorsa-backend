package com.pborsa.api.temporal.service;

import com.pborsa.api.config.temporal.TemporalProperties;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * Abstract base class providing conditional execution for Temporal workflows.
 * Equivalent to ImperioAwareService in reference project.
 * Services extending this class can use runWithTemporal() to conditionally execute workflows.
 */
@Slf4j
public abstract class TemporalAwareService {

    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    protected final TemporalProperties temporalProperties;

    public TemporalAwareService(TemporalProperties temporalProperties) {
        this.temporalProperties = temporalProperties;
    }

    /**
     * Executes the action if Temporal is enabled, otherwise logs a warning.
     *
     * @param action The action to execute if Temporal is enabled
     */
    protected void runWithTemporal(Runnable action) {
        runWithTemporalOrElse(action, () -> log.warn("Temporal integration is disabled, workflow will not be executed."));
    }

    /**
     * Executes the action if Temporal is enabled, otherwise executes the alternative.
     *
     * @param action     The action to execute if Temporal is enabled
     * @param alternative The alternative action to execute if Temporal is disabled
     */
    protected void runWithTemporalOrElse(Runnable action, Runnable alternative) {
        if (!temporalProperties.enabled()) {
            alternative.run();
            return;
        }
        action.run();
    }

    /**
     * Executes the supplier if Temporal is enabled, otherwise executes the alternative supplier.
     *
     * @param supplier   The supplier to execute if Temporal is enabled
     * @param alternative The alternative supplier to execute if Temporal is disabled
     * @param <T>        The return type
     * @return The result from supplier if enabled, result from alternative otherwise
     */
    protected <T> T runWithTemporalOrElse(Supplier<T> supplier, Supplier<T> alternative) {
        if (!temporalProperties.enabled()) {
            return alternative.get();
        }
        return supplier.get();
    }

    /**
     * Generates a current timestamp string for use in workflow IDs.
     *
     * @return Formatted timestamp string
     */
    protected String currentTimestamp() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }
}

