package com.pborsa.api.shared.config.temporal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public record TemporalProperties(
        @Value("${temporal.enabled:true}")
        boolean enabled,
        @Value("${temporal.workers.trading-worker.task-queue:TRADING_TASK_QUEUE}")
        String tradingQueueName,
        @Value("${temporal.workers.market-data-worker.task-queue:MARKET_DATA_TASK_QUEUE}")
        String marketDataQueueName
) {
}

