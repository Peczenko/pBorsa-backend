package com.pborsa.worker.temporal.workflow;

import com.pborsa.temporal.config.TaskQueues;
import com.pborsa.domain.dto.market.StockQuoteDto;
import com.pborsa.temporal.activity.TradingActivities;
import com.pborsa.temporal.workflow.MarketDataPollingWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.*;

/**
 * Implementation of the market data polling workflow.
 * Continuously fetches market data at specified intervals.
 */
public class MarketDataPollingWorkflowImpl implements MarketDataPollingWorkflow {

    private static final Logger log = Workflow.getLogger(MarketDataPollingWorkflowImpl.class);

    private final TradingActivities tradingActivities;
    
    private Set<String> symbols = new HashSet<>();
    private List<StockQuoteDto> latestQuotes = new ArrayList<>();
    private boolean running = true;

    public MarketDataPollingWorkflowImpl() {
        // Configure retry options for market data activities (shorter timeout, fewer retries)
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofSeconds(10))
                .setBackoffCoefficient(1.5)
                .setMaximumAttempts(2)
                .build();

        // Configure activity options with shorter timeout for market data operations
        ActivityOptions activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(2))
                .setTaskQueue(TaskQueues.MARKET_DATA_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();

        this.tradingActivities = Workflow.newActivityStub(TradingActivities.class, activityOptions);
    }

    @Override
    public void startPolling(Long userId, Set<String> initialSymbols, int intervalSeconds) {
        this.symbols = new HashSet<>(initialSymbols);
        
        while (running && !symbols.isEmpty()) {
            try {
                // Fetch quotes for all symbols
                latestQuotes = tradingActivities.getQuotes(userId, symbols);
            } catch (Exception e) {
                log.warn("Failed to fetch quotes: {}", e.getMessage());
            }
            
            // Wait for next polling interval
            Workflow.sleep(Duration.ofSeconds(intervalSeconds));
        }
    }

    @Override
    public void addSymbols(Set<String> newSymbols) {
        symbols.addAll(newSymbols);
    }

    @Override
    public void removeSymbols(Set<String> symbolsToRemove) {
        symbols.removeAll(symbolsToRemove);
    }

    @Override
    public void stopPolling() {
        running = false;
    }

    @Override
    public List<StockQuoteDto> getLatestQuotes() {
        return new ArrayList<>(latestQuotes);
    }

    @Override
    public Set<String> getCurrentSymbols() {
        return new HashSet<>(symbols);
    }

    @Override
    public boolean isPollingActive() {
        return running;
    }
}

