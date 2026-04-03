package com.pborsa.api.strategy.service;

import com.pborsa.domain.dto.strategy.BaseStrategyDto;
import com.pborsa.domain.dto.strategy.CreateUserStrategyRequest;
import com.pborsa.domain.dto.strategy.StrategyPnLDto;
import com.pborsa.domain.dto.strategy.UpdateUserStrategyRequest;
import com.pborsa.domain.dto.strategy.UserStrategyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Orchestrates strategy operations invoked by controllers.
 * Delegates to BaseStrategyService, UserStrategyCrudService,
 * and UserStrategyLifecycleService for specific operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyService {

    private final BaseStrategyService baseStrategyService;
    private final UserStrategyCrudService userStrategyCrudService;
    private final UserStrategyLifecycleService userStrategyLifecycleService;
    private final StrategyPnLService pnLService;

    // ==========================================
    // Base Strategy Operations (Read-only catalog)
    // ==========================================

    public List<BaseStrategyDto> getAllBaseStrategies() {
        return baseStrategyService.getAllActiveStrategies();
    }

    public Optional<BaseStrategyDto> getBaseStrategyByCode(String code) {
        return baseStrategyService.getStrategyByCode(code);
    }

    // ==========================================
    // User Strategy Operations (CRUD)
    // ==========================================

    public List<UserStrategyDto> getStrategiesByUserId(Long userId) {
        return userStrategyCrudService.getUserStrategies(userId);
    }

    public Optional<UserStrategyDto> getStrategyById(Long userId, Long strategyId) {
        return userStrategyCrudService.getUserStrategy(userId, strategyId);
    }

    public UserStrategyDto createUserStrategy(Long userId, CreateUserStrategyRequest request) {
        return userStrategyCrudService.createUserStrategy(userId, request);
    }

    public Optional<UserStrategyDto> updateUserStrategy(Long userId, Long strategyId, UpdateUserStrategyRequest request) {
        return userStrategyCrudService.updateUserStrategy(userId, strategyId, request);
    }

    public boolean deleteUserStrategy(Long userId, Long strategyId) {
        return userStrategyCrudService.deleteUserStrategy(userId, strategyId);
    }

    // ==========================================
    // Strategy Activation
    // ==========================================

    public Optional<UserStrategyDto> activateStrategy(Long userId, Long strategyId) {
        log.info("Activating strategy {} for user {}", strategyId, userId);
        return userStrategyLifecycleService.activateStrategy(userId, strategyId);
    }

    // ==========================================
    // Strategy P/L
    // ==========================================

    public Optional<StrategyPnLDto> getStrategyPnL(Long userId, Long strategyId) {
        log.debug("Getting P/L for strategy {} user {}", strategyId, userId);
        return pnLService.calculatePnL(userId, strategyId);
    }
}
