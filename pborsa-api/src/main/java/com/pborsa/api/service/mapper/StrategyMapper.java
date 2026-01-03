package com.pborsa.api.service.mapper;

import com.pborsa.api.domain.dto.strategy.StrategyDto;
import com.pborsa.api.domain.entity.StrategyEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting StrategyEntity to StrategyDto.
 * Single responsibility: Maps StrategyEntity ↔ StrategyDto only.
 */
@Component
public class StrategyMapper {

    /**
     * Maps a StrategyEntity to StrategyDto.
     *
     * @param entity Strategy entity
     * @return Strategy DTO
     */
    public StrategyDto toStrategyDto(StrategyEntity entity) {
        if (entity == null) {
            return null;
        }

        return new StrategyDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Maps a list of StrategyEntity to list of StrategyDto.
     *
     * @param entities List of strategy entities
     * @return List of strategy DTOs
     */
    public List<StrategyDto> toStrategyDtoList(List<StrategyEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toStrategyDto)
                .toList();
    }
}


