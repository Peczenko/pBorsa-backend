package com.pborsa.api.temporal.workflow;

import com.pborsa.api.domain.dto.market.StockQuoteDto;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;
import java.util.Set;

/**
 * Workflow for continuous market data polling.
 * Runs indefinitely, fetching market data at regular intervals.
 */
@WorkflowInterface
public interface MarketDataPollingWorkflow {

    /**
     * Starts the market data polling workflow.
     *
     * @param userId            The user ID
     * @param symbols           Initial symbols to poll
     * @param intervalSeconds   Polling interval in seconds
     */
    @WorkflowMethod
    void startPolling(String userId, Set<String> symbols, int intervalSeconds);

    /**
     * Signal to add symbols to the polling list.
     */
    @SignalMethod
    void addSymbols(Set<String> symbols);

    /**
     * Signal to remove symbols from the polling list.
     */
    @SignalMethod
    void removeSymbols(Set<String> symbols);

    /**
     * Signal to stop the polling workflow.
     */
    @SignalMethod
    void stopPolling();

    /**
     * Query to get the latest quotes.
     */
    @QueryMethod
    List<StockQuoteDto> getLatestQuotes();

    /**
     * Query to get currently polled symbols.
     */
    @QueryMethod
    Set<String> getCurrentSymbols();

    /**
     * Query to check if polling is active.
     */
    @QueryMethod
    boolean isPollingActive();
}

