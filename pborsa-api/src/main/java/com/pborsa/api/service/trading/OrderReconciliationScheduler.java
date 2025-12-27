package com.pborsa.api.service.trading;

import com.pborsa.api.domain.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderReconciliationScheduler {

    private final OrderReconciliationQueryService queryService;
    private final OrderReconciliationProcessor processor;
    private final Executor tradingExecutor;

    @Value("${orders.reconcile.exclude-recent-minutes:1}")
    private long excludeRecentMinutes;

    public OrderReconciliationScheduler(OrderReconciliationQueryService queryService,
                                        OrderReconciliationProcessor processor,
                                        @Qualifier("tradingExecutor") Executor tradingExecutor) {
        this.queryService = queryService;
        this.processor = processor;
        this.tradingExecutor = tradingExecutor;
    }

    @Scheduled(fixedDelayString = "${orders.reconcile.interval-ms:20000}")
    public void reconcile() {
        Instant now = Instant.now();
        Instant till = now.minus(Duration.ofMinutes(excludeRecentMinutes));

        List<OrderEntity> candidates = queryService.findCandidates(till);
        if (candidates.isEmpty()) {
            log.debug("Order reconciliation skipped, no candidates found");
            return;
        }

        Map<Long, List<OrderEntity>> byUser = candidates.stream()
                .collect(Collectors.groupingBy(OrderEntity::getUserId));

        for (var entry : byUser.entrySet()) {
            Long userId = entry.getKey();
            List<OrderEntity> orders = entry.getValue();
            tradingExecutor.execute(() -> processor.reconcileUser(userId, orders, till));
        }

        log.info("Order reconciliation scheduled users={} orders={} till={}",
                byUser.size(), candidates.size(), till);
    }
}
