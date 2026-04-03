package com.pborsa.api.order.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages per-order update queues to ensure order status updates are processed sequentially.
 * Each order (identified by alpacaOrderId or clientOrderId) gets its own queue,
 * preventing race conditions and ensuring updates are processed in the correct order.
 */
@Component
@Slf4j
public class OrderUpdateQueueManager {

    private final ConcurrentHashMap<String, ExecutorService> orderQueues = new ConcurrentHashMap<>();
    private final AtomicInteger queueCounter = new AtomicInteger(0);

    @Value("${alpaca.trade-updates.queue-cleanup-interval-seconds:300}")
    private long queueCleanupIntervalSeconds;

    private final ScheduledExecutorService cleanupScheduler;

    public OrderUpdateQueueManager() {
        this.cleanupScheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "order-update-queue-cleanup");
            t.setDaemon(true);
            return t;
        });
    }

    @PostConstruct
    private void init() {
        startCleanupTask();
    }

    /**
     * Submits an update task for processing in the order-specific queue.
     * Ensures updates for the same order are processed sequentially.
     *
     * @param orderKey Order identifier (alpacaOrderId or clientOrderId)
     * @param task     Task to execute
     */
    public void submitUpdate(String orderKey, Runnable task) {
        if (orderKey == null || orderKey.isBlank()) {
            log.warn("Cannot queue update with null or blank order key");
            task.run(); // Execute directly if no key available
            return;
        }

        ExecutorService queue = orderQueues.computeIfAbsent(orderKey, this::createOrderQueue);
        queue.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Error processing order update for key: {}", orderKey, e);
            }
        });
    }

    /**
     * Creates a single-threaded executor for a specific order.
     * This ensures all updates for this order are processed sequentially.
     */
    private ExecutorService createOrderQueue(String orderKey) {
        int queueNum = queueCounter.incrementAndGet();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, // Single thread per order
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> {
                    Thread t = new Thread(r, "order-update-" + queueNum + "-" + sanitizeOrderKey(orderKey));
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Block if queue is full
        );
        executor.allowCoreThreadTimeOut(true);
        log.debug("Created order update queue for key: {}", orderKey);
        return executor;
    }

    /**
     * Sanitizes order key for use in thread name.
     */
    private String sanitizeOrderKey(String orderKey) {
        if (orderKey == null) {
            return "unknown";
        }
        // Replace characters that might cause issues in thread names
        return orderKey.replaceAll("[^a-zA-Z0-9-]", "_").substring(0, Math.min(orderKey.length(), 20));
    }

    /**
     * Starts a periodic task to clean up idle order queues.
     */
    private void startCleanupTask() {
        cleanupScheduler.scheduleWithFixedDelay(
                this::cleanupIdleQueues,
                queueCleanupIntervalSeconds,
                queueCleanupIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    /**
     * Removes idle order queues that have no pending tasks.
     */
    private void cleanupIdleQueues() {
        int cleaned = 0;
        for (var entry : orderQueues.entrySet()) {
            ExecutorService queue = entry.getValue();
            if (queue instanceof ThreadPoolExecutor tpe) {
                // Remove queue if it has no active or queued tasks
                if (tpe.getActiveCount() == 0 && tpe.getQueue().isEmpty()) {
                    orderQueues.remove(entry.getKey());
                    tpe.shutdown();
                    cleaned++;
                }
            }
        }
        if (cleaned > 0) {
            log.debug("Cleaned up {} idle order update queues", cleaned);
        }
    }

    /**
     * Shuts down all order queues gracefully.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down order update queue manager");
        cleanupScheduler.shutdown();
        for (ExecutorService queue : orderQueues.values()) {
            queue.shutdown();
            try {
                if (!queue.awaitTermination(10, TimeUnit.SECONDS)) {
                    queue.shutdownNow();
                }
            } catch (InterruptedException e) {
                queue.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        orderQueues.clear();
    }
}
