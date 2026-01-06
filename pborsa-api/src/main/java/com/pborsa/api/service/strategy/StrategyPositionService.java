package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.trading.OrderSide;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.domain.entity.StrategyPositionEntity;
import com.pborsa.api.domain.entity.UserStrategyEntity;
import com.pborsa.api.domain.event.OrderStatusUpdatedEvent;
import com.pborsa.api.repository.OrderRepository;
import com.pborsa.api.repository.StrategyPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service for managing strategy positions.
 * Updates positions when orders are filled using the average cost method.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyPositionService {

    private final StrategyPositionRepository positionRepository;
    private final OrderRepository orderRepository;

    /**
     * Handles order status updates and updates positions when orders are filled.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderFilled(OrderStatusUpdatedEvent event) {
        if (event.status() != OrderStatus.FILLED) {
            return;
        }

        log.debug("Processing FILLED order event: orderId={}", event.orderId());

        // Fetch the order to get full details
        OrderEntity order = orderRepository.findById(event.orderId())
                .orElse(null);

        if (order == null) {
            log.warn("Order not found for FILLED event: orderId={}", event.orderId());
            return;
        }

        // Skip if order is not linked to a strategy
        if (order.getUserStrategy() == null) {
            log.debug("Order {} is not linked to a strategy, skipping position update", event.orderId());
            return;
        }

        Long strategyId = order.getUserStrategy().getId();
        BigDecimal filledQuantity = order.getFilledQuantity();
        BigDecimal filledAvgPrice = order.getFilledAvgPrice();
        OrderSide side = order.getSide();

        if (filledQuantity == null || filledAvgPrice == null || side == null) {
            log.warn("Missing fill data for order {}: qty={}, price={}, side={}",
                    event.orderId(), filledQuantity, filledAvgPrice, side);
            return;
        }

        updatePosition(strategyId, order.getUserStrategy(), side, filledQuantity, filledAvgPrice);
    }

    /**
     * Updates the position for a strategy based on a filled order.
     *
     * @param strategyId     User strategy ID
     * @param userStrategy   User strategy entity (for creating new positions)
     * @param side           Order side (BUY or SELL)
     * @param filledQuantity Filled quantity
     * @param filledAvgPrice Average fill price
     */
    @Transactional
    public void updatePosition(Long strategyId, 
                               UserStrategyEntity userStrategy,
                               OrderSide side, 
                               BigDecimal filledQuantity, 
                               BigDecimal filledAvgPrice) {
        StrategyPositionEntity position = getOrCreatePosition(strategyId, userStrategy);

        if (side == OrderSide.BUY) {
            position.applyBuy(filledQuantity, filledAvgPrice);
            log.info("Applied BUY to position: strategyId={}, qty={}, price={}, newTotalShares={}",
                    strategyId, filledQuantity, filledAvgPrice, position.getTotalShares());
        } else if (side == OrderSide.SELL) {
            BigDecimal realizedPnL = position.applySell(filledQuantity, filledAvgPrice);
            log.info("Applied SELL to position: strategyId={}, qty={}, price={}, realizedPnL={}, newTotalShares={}",
                    strategyId, filledQuantity, filledAvgPrice, realizedPnL, position.getTotalShares());
        }

        positionRepository.save(position);
    }

    /**
     * Gets the position for a strategy, creating one if it doesn't exist.
     */
    @Transactional
    public StrategyPositionEntity getOrCreatePosition(Long strategyId, UserStrategyEntity userStrategy) {
        Optional<StrategyPositionEntity> existing = positionRepository.findById(strategyId);
        if (existing.isPresent()) {
            return existing.get();
        }

        log.info("Creating new position for strategyId={}", strategyId);
        StrategyPositionEntity position = new StrategyPositionEntity()
                .setUserStrategy(userStrategy)
                .setTotalShares(BigDecimal.ZERO)
                .setTotalCostBasis(BigDecimal.ZERO)
                .setRealizedPnL(BigDecimal.ZERO);
        return positionRepository.save(position);
    }

    /**
     * Gets the current position for a strategy.
     *
     * @param strategyId User strategy ID
     * @return Position entity if exists
     */
    @Transactional(readOnly = true)
    public Optional<StrategyPositionEntity> getPosition(Long strategyId) {
        return positionRepository.findById(strategyId);
    }
}

