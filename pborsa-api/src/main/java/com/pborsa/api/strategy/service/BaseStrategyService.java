package com.pborsa.api.strategy.service;

import com.pborsa.domain.dto.strategy.BaseStrategyDto;
import com.pborsa.api.strategy.entity.BaseStrategyEntity;
import com.pborsa.api.strategy.repository.BaseStrategyRepository;
import com.pborsa.api.strategy.mapper.BaseStrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for base strategy catalog operations (read-only).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BaseStrategyService {

    private final BaseStrategyRepository baseStrategyRepository;
    private final BaseStrategyMapper baseStrategyMapper;

    /**
     * Gets all active base strategies.
     *
     * @return List of active base strategy DTOs
     */
    @Transactional(readOnly = true)
    public List<BaseStrategyDto> getAllActiveStrategies() {
        log.debug("Getting all active base strategies");
        List<BaseStrategyEntity> strategies = baseStrategyRepository.findByActiveTrue();
        return baseStrategyMapper.toBaseStrategyDtoList(strategies);
    }

    /**
     * Gets all base strategies (including inactive).
     *
     * @return List of all base strategy DTOs
     */
    @Transactional(readOnly = true)
    public List<BaseStrategyDto> getAllStrategies() {
        log.debug("Getting all base strategies");
        List<BaseStrategyEntity> strategies = baseStrategyRepository.findAll();
        return baseStrategyMapper.toBaseStrategyDtoList(strategies);
    }

    /**
     * Gets a base strategy by code.
     *
     * @param code Strategy code (e.g., MOMENTUM_V1)
     * @return Optional base strategy DTO
     */
    @Transactional(readOnly = true)
    public Optional<BaseStrategyDto> getStrategyByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Strategy code is required");
        }

        log.debug("Getting base strategy by code: {}", code);
        return baseStrategyRepository.findByCode(code.toUpperCase())
                .map(baseStrategyMapper::toBaseStrategyDto);
    }

    /**
     * Gets a base strategy entity by code.
     * Used internally by UserStrategyService.
     *
     * @param code Strategy code
     * @return Optional base strategy entity
     */
    @Transactional(readOnly = true)
    public Optional<BaseStrategyEntity> getStrategyEntityByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return baseStrategyRepository.findByCode(code.toUpperCase());
    }

    /**
     * Gets a base strategy by ID.
     *
     * @param id Strategy ID
     * @return Optional base strategy DTO
     */
    @Transactional(readOnly = true)
    public Optional<BaseStrategyDto> getStrategyById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        log.debug("Getting base strategy by ID: {}", id);
        return baseStrategyRepository.findById(id)
                .map(baseStrategyMapper::toBaseStrategyDto);
    }

    /**
     * Checks if a base strategy exists and is active.
     *
     * @param code Strategy code
     * @return true if strategy exists and is active
     */
    @Transactional(readOnly = true)
    public boolean isStrategyActiveByCode(String code) {
        return baseStrategyRepository.findByCode(code.toUpperCase())
                .map(BaseStrategyEntity::getActive)
                .orElse(false);
    }
}



