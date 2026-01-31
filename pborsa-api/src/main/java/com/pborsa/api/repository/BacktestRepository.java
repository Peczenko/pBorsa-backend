package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.BacktestEntity;
import com.pborsa.api.domain.entity.BacktestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for backtest entities.
 */
public interface BacktestRepository extends JpaRepository<BacktestEntity, Long> {

    /**
     * Finds all backtests for a user.
     *
     * @param userId User ID
     * @return List of backtests
     */
    List<BacktestEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds a specific backtest by user ID and backtest ID.
     *
     * @param id     Backtest ID
     * @param userId User ID
     * @return Optional backtest
     */
    Optional<BacktestEntity> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds all backtests for a user with a specific status.
     *
     * @param userId User ID
     * @param status Backtest status
     * @return List of backtests
     */
    List<BacktestEntity> findByUserIdAndStatus(Long userId, BacktestStatus status);

    /**
     * Finds all backtests with a specific status.
     *
     * @param status Backtest status
     * @return List of backtests
     */
    List<BacktestEntity> findByStatus(BacktestStatus status);
}
