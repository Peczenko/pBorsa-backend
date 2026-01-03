package com.pborsa.api.service.mapper;

import com.google.protobuf.Timestamp;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.domain.event.OrderStatusUpdatedEvent;
import com.pborsa.api.tradingengine.v1.OrderStatusUpdate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper for converting domain order status events to gRPC protobuf messages.
 */
@Component
public class OrderStatusProtoMapper {

    /**
     * Converts an OrderStatusUpdatedEvent to OrderStatusUpdate protobuf message.
     */
    public OrderStatusUpdate toProto(OrderStatusUpdatedEvent event) {
        if (event == null) {
            return null;
        }

        OrderStatusUpdate.Builder builder = OrderStatusUpdate.newBuilder()
                .setOrderId(event.orderId().toString())
                .setClientOrderId(event.clientOrderId())
                .setStatus(mapOrderStatus(event.status()))
                .setUpdatedAt(toTimestamp(event.updatedAt()))
                .setCreatedAt(toTimestamp(event.orderCreatedAt()))
                .setResumeToken(event.orderHistoryId().toString());

        if (event.workflowId() != null && !event.workflowId().isBlank()) {
            builder.setWorkflowId(event.workflowId());
        }
        if (event.alpacaOrderId() != null && !event.alpacaOrderId().isBlank()) {
            builder.setAlpacaOrderId(event.alpacaOrderId());
        }
        if (event.reason() != null) {
            builder.setReason(mapOrderStatusReason(event.reason()));
        }
        if (event.message() != null && !event.message().isBlank()) {
            builder.setMessage(event.message());
        }

        return builder.build();
    }

    private com.pborsa.api.tradingengine.v1.OrderStatus mapOrderStatus(OrderStatus status) {
        return switch (status) {
            case ACCEPTED_BY_APP -> com.pborsa.api.tradingengine.v1.OrderStatus.ACCEPTED_BY_APP;
            case NEW -> com.pborsa.api.tradingengine.v1.OrderStatus.NEW;
            case PARTIALLY_FILLED -> com.pborsa.api.tradingengine.v1.OrderStatus.PARTIALLY_FILLED;
            case FILLED -> com.pborsa.api.tradingengine.v1.OrderStatus.FILLED;
            case DONE_FOR_DAY -> com.pborsa.api.tradingengine.v1.OrderStatus.DONE_FOR_DAY;
            case CANCELED -> com.pborsa.api.tradingengine.v1.OrderStatus.CANCELLED;
            case CANCEL_REQUESTED -> com.pborsa.api.tradingengine.v1.OrderStatus.CANCELLED_REQUESTED;
            case EXPIRED -> com.pborsa.api.tradingengine.v1.OrderStatus.EXPIRED;
            case REPLACED -> com.pborsa.api.tradingengine.v1.OrderStatus.REPLACED;
            case PENDING_CANCEL -> com.pborsa.api.tradingengine.v1.OrderStatus.PENDING_CANCEL;
            case PENDING_REPLACE -> com.pborsa.api.tradingengine.v1.OrderStatus.PENDING_REPLACE;
            case REJECTED -> com.pborsa.api.tradingengine.v1.OrderStatus.REJECTED;
            case PENDING_NEW -> com.pborsa.api.tradingengine.v1.OrderStatus.PENDING_NEW;
            case ACCEPTED -> com.pborsa.api.tradingengine.v1.OrderStatus.ACCEPTED;
            case ACCEPTED_FOR_BIDDING -> com.pborsa.api.tradingengine.v1.OrderStatus.ACCEPTED_FOR_BIDDING;
            case STOPPED -> com.pborsa.api.tradingengine.v1.OrderStatus.STOPPED;
            case SUSPENDED -> com.pborsa.api.tradingengine.v1.OrderStatus.SUSPENDED;
            case CALCULATED -> com.pborsa.api.tradingengine.v1.OrderStatus.CALCULATED;
            case HELD -> com.pborsa.api.tradingengine.v1.OrderStatus.HELD;
        };
    }

    private com.pborsa.api.tradingengine.v1.OrderStatusReason mapOrderStatusReason(OrderStatusReason reason) {
        return switch (reason) {
            case INSUFFICIENT_FUNDS -> com.pborsa.api.tradingengine.v1.OrderStatusReason.INSUFFICIENT_FUNDS;
            case RATE_LIMIT -> com.pborsa.api.tradingengine.v1.OrderStatusReason.RATE_LIMIT;
            case INVALID_ORDER -> com.pborsa.api.tradingengine.v1.OrderStatusReason.INVALID_ORDER;
            case MARKET_CLOSED -> com.pborsa.api.tradingengine.v1.OrderStatusReason.MARKET_CLOSED;
            case AUTH_ERROR -> com.pborsa.api.tradingengine.v1.OrderStatusReason.AUTH_ERROR;
            case UNKNOWN -> com.pborsa.api.tradingengine.v1.OrderStatusReason.UNKNOWN;
        };
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}

