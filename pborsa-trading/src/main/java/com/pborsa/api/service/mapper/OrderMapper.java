package com.pborsa.api.service.mapper;

import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.domain.dto.trading.OrderSide;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderType;
import com.pborsa.api.domain.dto.trading.TimeInForce;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.openapi.trader.model.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Mapper for converting Alpaca order models to DTOs.
 */
@Component
@Slf4j
public class OrderMapper {

    /**
     * Converts an Alpaca Order object to OrderResponse.
     */
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .orderId(order.getId() != null ? order.getId().toString() : null)
                .clientOrderId(order.getClientOrderId())
                .symbol(order.getSymbol())
                .quantity(order.getQty() != null ? new BigDecimal(order.getQty()) : null)
                .filledQuantity(order.getFilledQty() != null ? new BigDecimal(order.getFilledQty()) : null)
                .side(parseOrderSide(order.getSide()))
                .type(parseOrderType(order.getType()))
                .timeInForce(parseTimeInForce(order.getTimeInForce()))
                .limitPrice(order.getLimitPrice() != null ? new BigDecimal(order.getLimitPrice()) : null)
                .stopPrice(order.getStopPrice() != null ? new BigDecimal(order.getStopPrice()) : null)
                .filledAveragePrice(order.getFilledAvgPrice() != null ? new BigDecimal(order.getFilledAvgPrice()) : null)
                .status(parseOrderStatus(order.getStatus()))
                .extendedHours(order.getExtendedHours())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toInstant() : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().toInstant() : null)
                .submittedAt(order.getSubmittedAt() != null ? order.getSubmittedAt().toInstant() : null)
                .filledAt(order.getFilledAt() != null ? order.getFilledAt().toInstant() : null)
                .cancelledAt(order.getCanceledAt() != null ? order.getCanceledAt().toInstant() : null)
                .expiredAt(order.getExpiredAt() != null ? order.getExpiredAt().toInstant() : null)
                .assetClass(order.getAssetClass() != null ? order.getAssetClass().name() : null)
                .build();
    }

    // Enum conversion helpers

    private OrderSide parseOrderSide(net.jacobpeterson.alpaca.openapi.trader.model.OrderSide side) {
        if (side == null) {
            return null;
        }
        try {
            return OrderSide.valueOf(side.name());
        } catch (Exception e) {
            log.warn("Unknown order side: {}", side);
            return null;
        }
    }

    private OrderType parseOrderType(net.jacobpeterson.alpaca.openapi.trader.model.OrderType type) {
        if (type == null) {
            return null;
        }
        try {
            return OrderType.valueOf(type.name());
        } catch (Exception e) {
            log.warn("Unknown order type: {}", type);
            return null;
        }
    }

    private TimeInForce parseTimeInForce(net.jacobpeterson.alpaca.openapi.trader.model.TimeInForce tif) {
        if (tif == null) {
            return null;
        }
        try {
            return TimeInForce.valueOf(tif.name());
        } catch (Exception e) {
            log.warn("Unknown time in force: {}", tif);
            return null;
        }
    }

    private OrderStatus parseOrderStatus(net.jacobpeterson.alpaca.openapi.trader.model.OrderStatus status) {
        if (status == null) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.name());
        } catch (Exception e) {
            log.warn("Unknown order status: {}", status);
            return null;
        }
    }
}
