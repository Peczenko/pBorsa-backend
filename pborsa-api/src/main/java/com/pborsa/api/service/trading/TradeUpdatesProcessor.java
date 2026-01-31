package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate.TradeUpdate;
import net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate.TradeUpdateEvent;
import net.jacobpeterson.alpaca.openapi.trader.model.Order;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeUpdatesProcessor {

    private final OrderPersistenceService orderPersistenceService;
    private final OrderMapper orderMapper;

    public void processUpdate(Long userId, TradeUpdate update) {
        Order order = update.getOrder();
        String alpacaOrderId = order != null ? order.getId() : null;
        String clientOrderId = order != null ? order.getClientOrderId() : null;
        OrderStatus status = resolveStatus(update, order);
        String message = buildMessage(update);

        if (status == null) {
            log.warn("Unable to resolve order status for user {} update {}", userId, update.getEvent());
            return;
        }

        // Convert Alpaca order to OrderResponse for fill data extraction
        OrderResponse fillData = order != null ? orderMapper.toOrderResponse(order) : null;

        boolean updated = orderPersistenceService.updateStatusByExternalIds(
                alpacaOrderId,
                clientOrderId,
                status,
                message,
                null,  // reason
                fillData  // fill data from Alpaca
        );
        if (updated) {
            log.info("Trade update applied user={} status={} orderId={} clientOrderId={} filledQty={} filledAvgPrice={}",
                    userId, status, alpacaOrderId, clientOrderId,
                    fillData != null ? fillData.filledQuantity() : null,
                    fillData != null ? fillData.filledAveragePrice() : null);
        }
    }

    private OrderStatus resolveStatus(TradeUpdate update, Order order) {
        if (order != null) {
            OrderResponse response = orderMapper.toOrderResponse(order);
            if (response != null && response.status() != null) {
                return response.status();
            }
        }
        TradeUpdateEvent event = update.getEvent();
        if (event == null) {
            return null;
        }
        return switch (event) {
            case NEW -> OrderStatus.NEW;
            case FILL -> OrderStatus.FILLED;
            case PARTIAL_FILL -> OrderStatus.PARTIALLY_FILLED;
            case CANCELED -> OrderStatus.CANCELED;
            case EXPIRED -> OrderStatus.EXPIRED;
            case DONE_FOR_DAY -> OrderStatus.DONE_FOR_DAY;
            case REPLACED -> OrderStatus.REPLACED;
            case REJECTED, ORDER_CANCEL_REJECTED, ORDER_REPLACE_REJECTED -> OrderStatus.REJECTED;
            case PENDING_NEW -> OrderStatus.PENDING_NEW;
            case STOPPED -> OrderStatus.STOPPED;
            case PENDING_CANCEL -> OrderStatus.PENDING_CANCEL;
            case PENDING_REPLACE -> OrderStatus.PENDING_REPLACE;
            case CALCULATED -> OrderStatus.CALCULATED;
            case SUSPENDED -> OrderStatus.SUSPENDED;
        };
    }

    private String buildMessage(TradeUpdate update) {
        TradeUpdateEvent event = update.getEvent();
        if (event == null) {
            return null;
        }
        return "trade_update:" + event.value();
    }
}
