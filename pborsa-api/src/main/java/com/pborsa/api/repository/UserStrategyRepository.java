package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.UserStrategyEntity;
import com.pborsa.api.domain.entity.UserStrategyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for user strategy subscriptions.
 */
public interface UserStrategyRepository extends JpaRepository<UserStrategyEntity, Long> {

    /**
     * Finds all strategies for a user.
     *
     * @param userId User ID
     * @return List of user strategies
     */
    List<UserStrategyEntity> findByUserId(Long userId);

    /**
     * Finds all strategies for a user with a specific status.
     *
     * @param userId User ID
     * @param status Strategy status
     * @return List of user strategies
     */
    List<UserStrategyEntity> findByUserIdAndStatus(Long userId, UserStrategyStatus status);

    /**
     * Finds a specific strategy by user ID and strategy ID.
     *
     * @param id     Strategy ID
     * @param userId User ID
     * @return Optional user strategy
     */
    Optional<UserStrategyEntity> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks if a user already has a strategy with the same base strategy, symbol, and specific status.
     *
     * @param userId         User ID
     * @param baseStrategyId Base strategy ID
     * @param symbol         Stock symbol
     * @param status         Strategy status
     * @return true if exists
     */
    boolean existsByUserIdAndBaseStrategyIdAndSymbolAndStatus(Long userId, Long baseStrategyId, String symbol, UserStrategyStatus status);

    /**
     * Checks if a user has a running (non-terminal) strategy with the same base strategy and symbol.
     * Running statuses are: CREATED, PREPARING, ACTIVE, PAUSED
     *
     * @param userId         User ID
     * @param baseStrategyId Base strategy ID
     * @param symbol         Stock symbol
     * @return true if a running strategy exists
     */
    @Query("SELECT CASE WHEN COUNT(us) > 0 THEN true ELSE false END " +
            "FROM UserStrategyEntity us WHERE us.userId = :userId " +
            "AND us.baseStrategy.id = :baseStrategyId " +
            "AND us.symbol = :symbol " +
            "AND us.status IN ('CREATED', 'PREPARING', 'ACTIVE', 'PAUSED')")
    boolean existsRunningStrategyByUserIdAndBaseStrategyIdAndSymbol(Long userId, Long baseStrategyId, String symbol);

    /**
     * Finds all active user strategies (for scheduling/execution).
     *
     * @return List of active user strategies
     */
    List<UserStrategyEntity> findByStatus(UserStrategyStatus status);

    /**
     * Finds all distinct symbols from active strategies.
     *
     * @return Set of unique symbols
     */
    @Query("SELECT DISTINCT us.symbol FROM UserStrategyEntity us WHERE us.status = 'ACTIVE'")
    Set<String> findDistinctSymbolsByActiveStatus();

    /**
     * Finds any active user ID (for system-level operations like price fetching).
     *
     * @return Optional user ID
     */
    @Query("SELECT us.userId FROM UserStrategyEntity us WHERE us.status = 'ACTIVE' ORDER BY us.id LIMIT 1")
    Optional<Long> findAnyActiveUserId();

    /**
     * Finds all active symbols with their strategy IDs.
     * Returns pairs of (symbol, strategyId) for bar streaming to trading engine.
     *
     * @return List of Object arrays where [0]=symbol (String), [1]=strategyId (Long)
     */
    @Query("SELECT us.symbol, us.id FROM UserStrategyEntity us WHERE us.status = 'ACTIVE'")
    List<Object[]> findActiveSymbolsWithStrategyIds();

    /**
     * Checks if a user has any strategies in ACTIVE or PREPARING status.
     * Used to prevent credential changes while strategies are running.
     *
     * @param userId User ID
     * @return true if user has active or preparing strategies
     */
    @Query("SELECT CASE WHEN COUNT(us) > 0 THEN true ELSE false END " +
            "FROM UserStrategyEntity us WHERE us.userId = :userId AND us.status IN ('ACTIVE', 'PREPARING')")
    boolean hasActiveOrPreparingStrategies(Long userId);
}



