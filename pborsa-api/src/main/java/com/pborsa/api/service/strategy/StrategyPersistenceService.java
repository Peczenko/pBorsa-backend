package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.entity.StrategyEntity;
import com.pborsa.api.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Persistence service for strategy data access.
 * Handles all database operations for strategies.
 * Single responsibility: Data access only, no business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyPersistenceService {

    private final StrategyRepository strategyRepository;

    /**
     * Finds all strategies.
     *
     * @return List of all strategies
     */
    @Transactional(readOnly = true)
    public List<StrategyEntity> findAllStrategies() {
        log.debug("Finding all strategies");
        return strategyRepository.findAll();
    }

    /**
     * Finds a strategy by ID.
     *
     * @param id Strategy ID
     * @return Optional strategy entity
     */
    @Transactional(readOnly = true)
    public Optional<StrategyEntity> findStrategyById(Long id) {
        log.debug("Finding strategy by ID: {}", id);
        return strategyRepository.findById(id);
    }
}


