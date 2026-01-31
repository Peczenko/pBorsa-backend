package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.BacktestOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for backtest order entities.
 */
public interface BacktestOrderRepository extends JpaRepository<BacktestOrderEntity, Long> {

    /**
     * Finds all orders for a backtest.
     *
     * @param backtestId Backtest ID
     * @return List of backtest orders ordered by execution time
     */
    List<BacktestOrderEntity> findByBacktestIdOrderByExecutedAtAsc(Long backtestId);
}
