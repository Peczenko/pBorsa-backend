package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.strategy.BaseStrategyDto;
import com.pborsa.api.domain.dto.strategy.CreateUserStrategyRequest;
import com.pborsa.api.domain.dto.strategy.UpdateUserStrategyRequest;
import com.pborsa.api.domain.dto.strategy.UserStrategyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Orchestrates strategy operations invoked by controllers.
 * Delegates to BaseStrategyService and UserStrategyService for specific operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyService {

    private final BaseStrategyService baseStrategyService;
    private final UserStrategyService userStrategyService;

    // ==========================================
    // Base Strategy Operations (Read-only catalog)
    // ==========================================

    /**
     * Gets all active base strategies from the catalog.
     *
     * @return List of active base strategy DTOs
     */
    public List<BaseStrategyDto> getAllBaseStrategies() {
        return baseStrategyService.getAllActiveStrategies();
    }

    /**
     * Gets a base strategy by code.
     *
     * @param code Strategy code (e.g., MOMENTUM_V1)
     * @return Optional base strategy DTO
     */
    public Optional<BaseStrategyDto> getBaseStrategyByCode(String code) {
        return baseStrategyService.getStrategyByCode(code);
    }

    // ==========================================
    // User Strategy Operations (CRUD)
    // ==========================================

    /**
     * Gets all strategies for a user.
     *
     * @param userId User ID
     * @return List of user strategy DTOs
     */
    public List<UserStrategyDto> getStrategiesByUserId(Long userId) {
        return userStrategyService.getUserStrategies(userId);
    }

    /**
     * Gets a specific user strategy by ID.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @return Optional user strategy DTO
     */
    public Optional<UserStrategyDto> getStrategyById(Long userId, Long strategyId) {
        return userStrategyService.getUserStrategy(userId, strategyId);
    }

    /**
     * Creates a new user strategy subscription.
     *
     * @param userId  User ID
     * @param request Create request
     * @return Created user strategy DTO
     */
    public UserStrategyDto createUserStrategy(Long userId, CreateUserStrategyRequest request) {
        return userStrategyService.createUserStrategy(userId, request);
    }

    /**
     * Updates an existing user strategy.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @param request    Update request
     * @return Updated user strategy DTO
     */
    public Optional<UserStrategyDto> updateUserStrategy(Long userId, Long strategyId, UpdateUserStrategyRequest request) {
        return userStrategyService.updateUserStrategy(userId, strategyId, request);
    }

    /**
     * Deletes a user strategy subscription.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @return true if deleted, false if not found
     */
    public boolean deleteUserStrategy(Long userId, Long strategyId) {
        return userStrategyService.deleteUserStrategy(userId, strategyId);
    }

    // ==========================================
    // Strategy Activation
    // ==========================================

    /**
     * Activates a user strategy, starting data transfer to the trading engine.
     * The strategy must be in CREATED status.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @return Updated user strategy DTO (status: PREPARING)
     */
    public Optional<UserStrategyDto> activateStrategy(Long userId, Long strategyId) {
        log.info("Activating strategy {} for user {}", strategyId, userId);
        return userStrategyService.activateStrategy(userId, strategyId);
    }
}
