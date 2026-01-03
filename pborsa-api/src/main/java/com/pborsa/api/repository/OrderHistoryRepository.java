package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.OrderHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntity, UUID> {
    
    Optional<OrderHistoryEntity> findById(UUID id);
    
    @Query("""
            SELECT h FROM OrderHistoryEntity h
            WHERE h.userId = :userId
            AND h.createdAt > :afterTimestamp
            ORDER BY h.createdAt ASC
            """)
    List<OrderHistoryEntity> findByUserIdAndCreatedAtAfter(@Param("userId") Long userId, 
                                                           @Param("afterTimestamp") Instant afterTimestamp);

    @Query("""
            SELECT h FROM OrderHistoryEntity h
            WHERE h.order.id = :orderId
            ORDER BY h.createdAt ASC
            """)
    List<OrderHistoryEntity> findByOrderIdOrderByCreatedAtAsc(@Param("orderId") UUID orderId);

    @Query("""
            SELECT h FROM OrderHistoryEntity h
            WHERE h.userId = :userId
            AND h.order.id = :orderId
            ORDER BY h.createdAt ASC
            """)
    List<OrderHistoryEntity> findByUserIdAndOrderId(@Param("userId") Long userId, @Param("orderId") UUID orderId);
}
