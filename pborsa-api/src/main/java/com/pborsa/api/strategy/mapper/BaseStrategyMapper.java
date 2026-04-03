package com.pborsa.api.strategy.mapper;

import com.pborsa.domain.dto.strategy.BaseStrategyDto;
import com.pborsa.api.strategy.entity.BaseStrategyEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting BaseStrategyEntity to BaseStrategyDto.
 */
@Component
public class BaseStrategyMapper {

    /**
     * Maps a BaseStrategyEntity to BaseStrategyDto.
     *
     * @param entity Base strategy entity
     * @return Base strategy DTO
     */
    public BaseStrategyDto toBaseStrategyDto(BaseStrategyEntity entity) {
        if (entity == null) {
            return null;
        }

        return new BaseStrategyDto(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Maps a list of BaseStrategyEntity to list of BaseStrategyDto.
     *
     * @param entities List of base strategy entities
     * @return List of base strategy DTOs
     */
    public List<BaseStrategyDto> toBaseStrategyDtoList(List<BaseStrategyEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toBaseStrategyDto)
                .toList();
    }
}



