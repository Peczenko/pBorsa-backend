package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.domain.entity.OrderHistoryEntity;
import com.pborsa.api.repository.OrderHistoryRepository;
import com.pborsa.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    private OrderEntity createOrder(String userId,
                                    TradingApiOrderRequest request,
                                    OrderStatus status,
                                    String workflowId) {
        OrderEntity entity = new OrderEntity()
                .setUserId(userId)
                .setWorkflowId(workflowId)
                .setClientOrderId(request.clientOrderId())
                .setSymbol(request.symbol())
                .setSide(request.side())
                .setType(request.type())
                .setTimeInForce(request.timeInForce())
                .setQuantity(request.quantity())
                .setLimitPrice(request.limitPrice())
                .setStopPrice(request.stopPrice())
                .setStatus(status)
                .setExtendedHours(request.extendedHours());

        return orderRepository.save(entity);
    }

    public void createOrderHistory(String userId, OrderEntity entity, OrderStatus status, String message) {
        OrderHistoryEntity history = new OrderHistoryEntity()
                .setOrder(entity)
                .setUserId(userId)
                .setStatus(status)
                .setMessage(message);

        orderHistoryRepository.save(history);
    }

    public OrderEntity updateFromResponse(OrderEntity entity, OrderResponse response) {
        entity.setAlpacaOrderId(response.orderId());
        entity.setClientOrderId(response.clientOrderId());
        entity.setStatus(response.status());
        entity.setFilledQuantity(response.filledQuantity());
        entity.setLimitPrice(response.limitPrice());
        entity.setStopPrice(response.stopPrice());
        entity.setTimeInForce(response.timeInForce());
        entity.setSide(response.side());
        entity.setType(response.type());
        entity.setQuantity(response.quantity());
        entity.setExtendedHours(response.extendedHours());
        entity.setCreatedAtRemote(response.createdAt());
        entity.setUpdatedAtRemote(response.updatedAt());
        entity.setSubmittedAt(response.submittedAt());
        entity.setFilledAt(response.filledAt());
        entity.setExpiredAt(response.expiredAt());
        entity.setCancelledAt(response.cancelledAt());
        return orderRepository.save(entity);
    }

    public Optional<OrderEntity> findById(UUID id) {
        return orderRepository.findById(id);
    }

    public OrderEntity markStatus(OrderEntity entity, OrderStatus status) {
        entity.setStatus(status);
        return orderRepository.save(entity);
    }

    public OrderEntity updateStatus(UUID orderId, OrderStatus status, String message) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        entity.setStatus(status);
        OrderEntity saved = orderRepository.save(entity);
        createOrderHistory(entity.getUserId(), saved, status, message);
        return saved;
    }

    public OrderEntity createNewOrder(String userId, TradingApiOrderRequest request, String workflowId) {
        OrderEntity entity = createOrder(userId, request, OrderStatus.NEW, workflowId);
        createOrderHistory(userId, entity, OrderStatus.PENDING_NEW, null);
        return entity;
    }

    public OrderEntity createRejectedOrder(String userId, TradingApiOrderRequest request, String message) {
        OrderEntity entity = createOrder(userId, request, OrderStatus.REJECTED, null);
        createOrderHistory(userId, entity, OrderStatus.REJECTED, message);
        return entity;
    }
}
