package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.domain.entity.OrderHistoryEntity;
import com.pborsa.api.domain.entity.StrategyEntity;
import com.pborsa.api.repository.OrderHistoryRepository;
import com.pborsa.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for persisting orders and order history.
 * Follows single responsibility principle - only handles database operations.
 * Event publishing is delegated to OrderStatusEventPublisher.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPersistenceService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderStatusEventPublisher eventPublisher;

    private OrderEntity createOrder(Long userId,
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

        Long strategyId = request.strategyId();
        if (strategyId != null) {
            entity.setStrategy(new StrategyEntity().setId(strategyId));
        }

        return orderRepository.save(entity);
    }

    public void createOrderHistory(Long userId, OrderEntity entity, OrderStatus status, String message) {
        createOrderHistory(userId, entity, status, message, null);
    }

    public OrderHistoryEntity createOrderHistory(Long userId,
                                   OrderEntity entity,
                                   OrderStatus status,
                                   String message,
                                   OrderStatusReason reason) {
        OrderHistoryEntity history = new OrderHistoryEntity()
                .setOrder(entity)
                .setUserId(userId)
                .setStatus(status)
                .setReason(reason)
                .setMessage(message);

        OrderHistoryEntity saved = orderHistoryRepository.save(history);
        
        // Publish event for gRPC notifications
        eventPublisher.publishStatusUpdateEvent(entity, saved, status, message, reason);
        
        return saved;
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


    public OrderEntity markStatus(OrderEntity entity, OrderStatus status) {
        entity.setStatus(status);
        return orderRepository.save(entity);
    }

    public OrderEntity updateStatus(UUID orderId, OrderStatus status, String message) {
        return updateStatus(orderId, status, message, null);
    }

    public OrderEntity updateStatus(UUID orderId,
                                    OrderStatus status,
                                    String message,
                                    OrderStatusReason reason) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (entity.getStatus() == status && message == null) {
            return entity;
        }
        entity.setStatus(status);
        OrderEntity saved = orderRepository.save(entity);
        // createOrderHistory will publish the event
        createOrderHistory(entity.getUserId(), saved, status, message, reason);
        return saved;
    }

    public boolean updateStatusByExternalIds(String alpacaOrderId,
                                             String clientOrderId,
                                             OrderStatus status,
                                             String message) {
        return updateStatusByExternalIds(alpacaOrderId, clientOrderId, status, message, null);
    }

    public boolean updateStatusByExternalIds(String alpacaOrderId,
                                             String clientOrderId,
                                             OrderStatus status,
                                             String message,
                                             OrderStatusReason reason) {
        Optional<OrderEntity> entity = findByExternalIds(alpacaOrderId, clientOrderId);
        if (entity.isEmpty()) {
            log.warn("Order not found for alpacaOrderId={} clientOrderId={}", alpacaOrderId, clientOrderId);
            return false;
        }
        OrderEntity order = entity.get();
        if (order.getAlpacaOrderId() == null && alpacaOrderId != null) {
            order.setAlpacaOrderId(alpacaOrderId);
        }
        if (order.getClientOrderId() == null && clientOrderId != null) {
            order.setClientOrderId(clientOrderId);
        }
        if (order.getStatus() == status && message == null) {
            return true;
        }
        order.setStatus(status);
        OrderEntity saved = orderRepository.save(order);
        // createOrderHistory will publish the event
        createOrderHistory(order.getUserId(), saved, status, message, reason);
        return true;
    }

    /**
     * Finds order by external IDs (alpacaOrderId or clientOrderId).
     * Used internally for status updates.
     */
    private Optional<OrderEntity> findByExternalIds(String alpacaOrderId, String clientOrderId) {
        if (alpacaOrderId != null && !alpacaOrderId.isBlank()) {
            Optional<OrderEntity> byAlpaca = orderRepository.findByAlpacaOrderId(alpacaOrderId);
            if (byAlpaca.isPresent()) {
                return byAlpaca;
            }
        }
        if (clientOrderId != null && !clientOrderId.isBlank()) {
            return orderRepository.findByClientOrderId(clientOrderId);
        }
        return Optional.empty();
    }


    public OrderEntity createNewOrder(Long userId, TradingApiOrderRequest request, String workflowId) {
        OrderEntity entity = createOrder(userId, request, OrderStatus.ACCEPTED_BY_APP, workflowId);
        createOrderHistory(userId, entity, OrderStatus.ACCEPTED_BY_APP, null);
        return entity;
    }

    public OrderEntity createRejectedOrder(Long userId, TradingApiOrderRequest request, String message) {
        OrderEntity entity = createOrder(userId, request, OrderStatus.REJECTED, null);
        createOrderHistory(userId, entity, OrderStatus.REJECTED, message);
        return entity;
    }

    /**
     * Finds orders by user ID.
     * Security: Always filters by userId to ensure user scoping.
     *
     * @param userId User ID
     * @return List of orders for the user
     */
    public List<OrderEntity> findOrdersByUserId(Long userId) {
        log.debug("Finding orders by user ID: {}", userId);
        return orderRepository.findByUserId(userId);
    }

    /**
     * Finds orders by user ID and strategy ID.
     * Security: Always filters by userId to ensure user scoping.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @return List of orders for the user and strategy
     */
    public List<OrderEntity> findOrdersByUserIdAndStrategyId(Long userId, Long strategyId) {
        log.debug("Finding orders by user ID: {} and strategy ID: {}", userId, strategyId);
        return orderRepository.findByUserIdAndStrategyId(userId, strategyId);
    }

    /**
     * Finds an order by ID.
     * Note: This method does not filter by userId - use with caution.
     * Prefer methods that include userId for user-scoped queries.
     *
     * @param orderId Order ID
     * @return Optional order entity
     */
    public Optional<OrderEntity> findOrderById(UUID orderId) {
        log.debug("Finding order by ID: {}", orderId);
        return orderRepository.findById(orderId);
    }
}
