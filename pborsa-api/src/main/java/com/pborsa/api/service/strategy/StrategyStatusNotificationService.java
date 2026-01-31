package com.pborsa.api.service.strategy;

import com.pborsa.api.client.tradingengine.TradingEngineOrderStatusClient;
import com.pborsa.api.domain.entity.UserStrategyStatus;
import com.pborsa.api.domain.event.StrategyStatusChangedEvent;
import com.pborsa.api.service.mapper.StrategyStatusProtoMapper;
import com.pborsa.api.tradingengine.v1.StrategyStatusNotification;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service that listens to strategy status change events and notifies the trading engine via gRPC.
 *
 * This service is responsible for:
 * - Listening to StrategyStatusChangedEvent when strategy transitions to STOPPED
 * - Converting domain events to protobuf messages
 * - Delegating gRPC calls to TradingEngineOrderStatusClient
 * - Handling errors gracefully (notification failures don't affect strategy updates)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(TradingEngineOrderStatusClient.class)
public class StrategyStatusNotificationService {

    private final TradingEngineOrderStatusClient tradingEngineClient;
    private final StrategyStatusProtoMapper protoMapper;

    @EventListener
    @Async
    public void onStrategyStatusChanged(StrategyStatusChangedEvent event) {
        if (event == null) {
            return;
        }

        // Only notify trading engine when strategy is stopped
        if (event.newStatus() != UserStrategyStatus.STOPPED) {
            log.debug("Strategy {} status changed to {}, skipping notification (only STOPPED triggers notification)",
                    event.strategyId(), event.newStatus());
            return;
        }

        Optional<StrategyStatusNotification> notificationOpt = buildNotification(event);
        if (notificationOpt.isEmpty()) {
            log.warn("Failed to build strategy status notification for strategyId={}", event.strategyId());
            return;
        }

        notifyTradingEngine(notificationOpt.get(), event);
    }

    private Optional<StrategyStatusNotification> buildNotification(StrategyStatusChangedEvent event) {
        try {
            StrategyStatusNotification notification = protoMapper.toProto(event);
            return Optional.ofNullable(notification);
        } catch (Exception e) {
            log.error("Failed to convert strategy status event to proto strategyId={}", event.strategyId(), e);
            return Optional.empty();
        }
    }

    private void notifyTradingEngine(StrategyStatusNotification notification, StrategyStatusChangedEvent event) {
        try {
            log.debug("Notifying trading engine about strategy status update strategyId={} userId={} status={}",
                    event.strategyId(), event.userId(), event.newStatus());

            var ack = tradingEngineClient.notifyStrategyStatusUpdate(notification);

            log.info("Trading engine acknowledged strategy status update strategyId={} received={}",
                    event.strategyId(), ack.getReceived());

        } catch (StatusRuntimeException e) {
            log.error("Failed to notify trading engine about strategy status update strategyId={} userId={}: {} - {}",
                    event.strategyId(), event.userId(), e.getStatus().getCode(), e.getStatus().getDescription());
            // Don't throw - we don't want to fail the strategy update if notification fails
        } catch (Exception e) {
            log.error("Unexpected error notifying trading engine about strategy status update strategyId={} userId={}",
                    event.strategyId(), event.userId(), e);
        }
    }
}
