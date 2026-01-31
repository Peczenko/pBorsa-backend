package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findByUpdatedAtBeforeAndStatusNotIn(Instant before,
                                                          Collection<OrderStatus> terminalStatuses);

    Optional<OrderEntity> findByAlpacaOrderId(String alpacaOrderId);

    Optional<OrderEntity> findByClientOrderId(String clientOrderId);

    long countByUserIdAndStatusNotIn(Long userId, Collection<OrderStatus> terminalStatuses);

    @Query("""
            select distinct o.userId
            from OrderEntity o
            where o.status not in :terminalStatuses
              and o.userId is not null
            """)
    List<Long> findDistinctUserIdByStatusNotIn(@Param("terminalStatuses") Collection<OrderStatus> terminalStatuses);

    List<OrderEntity> findByUserId(Long userId);

    List<OrderEntity> findByUserIdAndUserStrategyId(Long userId, Long userStrategyId);

    List<OrderEntity> findByUserStrategyIdAndStatus(Long userStrategy_id, OrderStatus status);
}
