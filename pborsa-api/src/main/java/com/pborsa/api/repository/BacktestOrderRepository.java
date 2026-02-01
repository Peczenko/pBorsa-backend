package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.BacktestOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for backtest order entities.
 */
public interface BacktestOrderRepository extends JpaRepository<BacktestOrderEntity, Long> {

    /**
     * Projection for backtest order counts grouped by backtest ID.
     */
    interface BacktestOrderCounts {
        Long getBacktestId();
        Long getBuyCount();
        Long getSellCount();
    }

    /**
     * Finds all orders for a backtest.
     *
     * @param backtestId Backtest ID
     * @return List of backtest orders ordered by execution time
     */
    List<BacktestOrderEntity> findByBacktestIdOrderByExecutedAtAsc(Long backtestId);

    /**
     * Finds orders for a backtest with paging.
     *
     * @param backtestId Backtest ID
     * @param pageable   Paging parameters
     * @return Page of backtest orders
     */
    Page<BacktestOrderEntity> findByBacktestId(Long backtestId, Pageable pageable);

    /**
     * Counts BUY and SELL orders for the given backtest IDs.
     *
     * @param backtestIds Backtest IDs
     * @return List of counts grouped by backtest ID
     */
    @Query("""
            select o.backtest.id as backtestId,
                   sum(case when upper(o.side) = 'BUY' then 1 else 0 end) as buyCount,
                   sum(case when upper(o.side) = 'SELL' then 1 else 0 end) as sellCount
            from BacktestOrderEntity o
            where o.backtest.id in :backtestIds
            group by o.backtest.id
            """)
    List<BacktestOrderCounts> findOrderCountsByBacktestIds(@Param("backtestIds") List<Long> backtestIds);
}
