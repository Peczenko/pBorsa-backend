package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.strategy.StrategyPnLDto;
import com.pborsa.api.domain.entity.StrategyPositionEntity;
import com.pborsa.api.domain.entity.UserStrategyEntity;
import com.pborsa.api.repository.UserStrategyRepository;
import com.pborsa.api.service.market.MarketPriceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service for calculating and retrieving strategy P/L.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyPnLService {

    private final StrategyPositionService positionService;
    private final MarketPriceCacheService marketPriceCacheService;
    private final UserStrategyRepository userStrategyRepository;

    /**
     * Calculates the current P/L for a user strategy.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @return P/L data, or empty if strategy not found
     */
    @Transactional(readOnly = true)
    public Optional<StrategyPnLDto> calculatePnL(Long userId, Long strategyId) {
        // Get the user strategy
        Optional<UserStrategyEntity> strategyOpt = userStrategyRepository.findByIdAndUserId(strategyId, userId);
        if (strategyOpt.isEmpty()) {
            log.debug("Strategy {} not found for user {}", strategyId, userId);
            return Optional.empty();
        }

        UserStrategyEntity strategy = strategyOpt.get();
        String symbol = strategy.getSymbol();

        // Get position data
        Optional<StrategyPositionEntity> positionOpt = positionService.getPosition(strategyId);
        if (positionOpt.isEmpty()) {
            log.debug("No position found for strategy {}, returning empty P/L", strategyId);
            return Optional.of(StrategyPnLDto.empty(strategyId, symbol));
        }

        StrategyPositionEntity position = positionOpt.get();

        // Get current market price for unrealized P/L
        BigDecimal currentPrice = marketPriceCacheService.getCurrentPrice(symbol, userId)
                .orElse(null);

        // Calculate unrealized P/L
        BigDecimal unrealizedPnL = BigDecimal.ZERO;
        if (currentPrice != null && position.getTotalShares().compareTo(BigDecimal.ZERO) > 0) {
            unrealizedPnL = position.calculateUnrealizedPnL(currentPrice);
        }

        return Optional.of(StrategyPnLDto.of(
                strategyId,
                symbol,
                position.getRealizedPnL(),
                unrealizedPnL,
                position.getTotalShares(),
                position.getAverageCostPerShare(),
                currentPrice,
                position.getTotalCostBasis(),
                position.getUpdatedAt()
        ));
    }

    /**
     * Gets P/L for a strategy without requiring user validation.
     * Used internally or for admin access.
     *
     * @param strategyId Strategy ID
     * @return P/L data, or empty if strategy not found
     */
    @Transactional(readOnly = true)
    public Optional<StrategyPnLDto> calculatePnLInternal(Long strategyId) {
        // Get the user strategy
        Optional<UserStrategyEntity> strategyOpt = userStrategyRepository.findById(strategyId);
        if (strategyOpt.isEmpty()) {
            log.debug("Strategy {} not found", strategyId);
            return Optional.empty();
        }

        UserStrategyEntity strategy = strategyOpt.get();
        return calculatePnL(strategy.getUserId(), strategyId);
    }
}

