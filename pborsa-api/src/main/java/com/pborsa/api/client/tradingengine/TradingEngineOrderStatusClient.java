package com.pborsa.api.client.tradingengine;

import com.pborsa.api.tradingengine.v1.OrderStatusUpdate;
import com.pborsa.api.tradingengine.v1.OrderStatusUpdateAck;
import com.pborsa.api.tradingengine.v1.StrategyStatusNotification;
import com.pborsa.api.tradingengine.v1.StrategyStatusNotificationAck;
import com.pborsa.api.tradingengine.v1.TradingEngineServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * gRPC client for sending order status updates to the trading engine.
 * This client connects to the TradingEngineService (port 9090) to notify about order status changes.
 */
@Slf4j
public class TradingEngineOrderStatusClient implements AutoCloseable {

    private final ManagedChannel channel;
    private final TradingEngineServiceGrpc.TradingEngineServiceBlockingStub blockingStub;
    private final int timeoutSeconds;

    public TradingEngineOrderStatusClient(String targetAddress) {
        this(targetAddress, 5);
    }

    public TradingEngineOrderStatusClient(String targetAddress, int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.channel = ManagedChannelBuilder.forTarget(targetAddress)
                .usePlaintext()
                .build();
        this.blockingStub = TradingEngineServiceGrpc.newBlockingStub(channel);
        log.info("TradingEngineOrderStatusClient initialized for {}", targetAddress);
    }

    /**
     * Notifies the trading engine about an order status update.
     *
     * @param update The order status update to send
     * @return Acknowledgment from trading engine
     * @throws StatusRuntimeException if the gRPC call fails
     */
    public OrderStatusUpdateAck notifyOrderStatusUpdate(OrderStatusUpdate update) {
        if (update == null) {
            throw new IllegalArgumentException("OrderStatusUpdate cannot be null");
        }

        return blockingStub
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                .notifyOrderStatusUpdate(update);
    }

    /**
     * Notifies the trading engine about a strategy status update.
     *
     * @param notification The strategy status notification to send
     * @return Acknowledgment from trading engine
     * @throws StatusRuntimeException if the gRPC call fails
     */
    public StrategyStatusNotificationAck notifyStrategyStatusUpdate(StrategyStatusNotification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("StrategyStatusNotification cannot be null");
        }

        return blockingStub
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                .notifyStrategyStatusUpdate(notification);
    }

    @Override
    public void close() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
            log.info("TradingEngineOrderStatusClient closed");
        }
    }
}

