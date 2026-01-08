package com.pborsa.api.repository;

import com.pborsa.api.domain.entity.StrategyPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for strategy position tracking.
 */
@Repository
public interface StrategyPositionRepository extends JpaRepository<StrategyPositionEntity, Long> {

    /**
     * Finds all positions for a specific user (via user strategies).
     *
     * @param userId User ID
     * @return List of positions
     */
    List<StrategyPositionEntity> findByUserStrategy_UserId(Long userId);
}

