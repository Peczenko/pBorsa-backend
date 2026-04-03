package com.pborsa.api.strategy.repository;

import com.pborsa.api.strategy.entity.BaseStrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for base strategy templates (read-only catalog).
 */
public interface BaseStrategyRepository extends JpaRepository<BaseStrategyEntity, Long> {

    /**
     * Finds a base strategy by its unique code.
     *
     * @param code Strategy code (e.g., MOMENTUM_V1)
     * @return Optional base strategy
     */
    Optional<BaseStrategyEntity> findByCode(String code);

    /**
     * Finds all active base strategies.
     *
     * @return List of active base strategies
     */
    List<BaseStrategyEntity> findByActiveTrue();
}



