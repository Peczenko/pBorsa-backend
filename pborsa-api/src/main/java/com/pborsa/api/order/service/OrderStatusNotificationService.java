package com.pborsa.api.order.service;

import com.pborsa.api.tradingengine.TradingEngineOrderStatusClient;
import com.pborsa.domain.event.OrderStatusUpdatedEvent;
import com.pborsa.api.order.mapper.OrderStatusProtoMapper;
import com.pborsa.api.tradingengine.v1.OrderStatusUpdate;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service that listens to order status update events and notifies the trading engine via gRPC.
 * 
 * This service is responsible for:
 * - Listening to OrderStatusUpdatedEvent
 * - Converting domain events to protobuf messages
 * - Delegating gRPC calls to TradingEngineOrderStatusClient
 * - Handling errors gracefully (notification failures don't affect order updates)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(TradingEngineOrderStatusClient.class)
public class OrderStatusNotificationService {

    private final TradingEngineOrderStatusClient orderStatusClient;
    private final OrderStatusProtoMapper protoMapper;

    @EventListener
    @Async
    public void onOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        if (event == null) {
            return;
        }

        String clientOrderId = event.clientOrderId();
        if (clientOrderId == null || clientOrderId.isBlank()) {
            log.debug("Order status update has no client_order_id, skipping notification orderId={}", 
                    event.orderId());
            return;
        }

        Optional<OrderStatusUpdate> updateOpt = buildUpdate(event);
        if (updateOpt.isEmpty()) {
            log.warn("Failed to build order status update for orderId={}", event.orderId());
            return;
        }

        notifyTradingEngine(updateOpt.get(), clientOrderId, event.orderId());
    }

    private Optional<OrderStatusUpdate> buildUpdate(OrderStatusUpdatedEvent event) {
        try {
            OrderStatusUpdate update = protoMapper.toProto(event);
            return Optional.ofNullable(update);
        } catch (Exception e) {
            log.error("Failed to convert order status event to proto orderId={}", event.orderId(), e);
            return Optional.empty();
        }
    }

    private void notifyTradingEngine(OrderStatusUpdate update, String clientOrderId, java.util.UUID orderId) {
        try {
            log.debug("Notifying trading engine about order status update clientOrderId={} orderId={} status={}", 
                    clientOrderId, orderId, update.getStatus());

            var ack = orderStatusClient.notifyOrderStatusUpdate(update);

            log.debug("Trading engine acknowledged order status update clientOrderId={} received={}", 
                    clientOrderId, ack.getReceived());

        } catch (StatusRuntimeException e) {
            log.error("Failed to notify trading engine about order status update clientOrderId={} orderId={}: {} - {}", 
                    clientOrderId, orderId, e.getStatus().getCode(), e.getStatus().getDescription());
            // Don't throw - we don't want to fail the order update if notification fails
        } catch (Exception e) {
            log.error("Unexpected error notifying trading engine about order status update clientOrderId={} orderId={}", 
                    clientOrderId, orderId, e);
        }
    }
}

