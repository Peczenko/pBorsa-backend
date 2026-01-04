package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.UserStrategyEntity;
import com.pborsa.api.domain.entity.UserStrategyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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
     * Checks if a user already has a strategy with the same base strategy and symbol.
     *
     * @param userId         User ID
     * @param baseStrategyId Base strategy ID
     * @param symbol         Stock symbol
     * @return true if exists
     */
    boolean existsByUserIdAndBaseStrategyIdAndSymbol(Long userId, Long baseStrategyId, String symbol);

    /**
     * Finds all active user strategies (for scheduling/execution).
     *
     * @return List of active user strategies
     */
    List<UserStrategyEntity> findByStatus(UserStrategyStatus status);
}



