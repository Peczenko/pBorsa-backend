package com.pborsa.api.temporal.workflow;

import java.util.List;
import java.util.Set;

import com.pborsa.api.domain.dto.market.StockQuoteDto;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for market data polling.
 * Continuously fetches market data at specified intervals.
 */
@WorkflowInterface
public interface MarketDataPollingWorkflow {

    /**
     * Starts polling market data for the given symbols.
     *
     * @param userId          The user ID
     * @param initialSymbols Set of symbols to poll
     * @param intervalSeconds Polling interval in seconds
     */
    @WorkflowMethod
    void startPolling(String userId, Set<String> initialSymbols, int intervalSeconds);

    /**
     * Adds symbols to the polling set.
     */
    @SignalMethod
    void addSymbols(Set<String> newSymbols);

    /**
     * Removes symbols from the polling set.
     */
    @SignalMethod
    void removeSymbols(Set<String> symbolsToRemove);

    /**
     * Stops polling.
     */
    @SignalMethod
    void stopPolling();

    /**
     * Gets the latest quotes.
     */
    @QueryMethod
    List<StockQuoteDto> getLatestQuotes();

    /**
     * Gets the current symbols being polled.
     */
    @QueryMethod
    Set<String> getCurrentSymbols();

    /**
     * Checks if polling is active.
     */
    @QueryMethod
    boolean isPollingActive();
}

