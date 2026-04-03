package com.pborsa.api.strategy.mapper;

import com.pborsa.domain.dto.strategy.UserStrategyDto;
import com.pborsa.api.strategy.entity.UserStrategyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting UserStrategyEntity to UserStrategyDto.
 */
@Component
@RequiredArgsConstructor
public class UserStrategyMapper {

    private final BaseStrategyMapper baseStrategyMapper;

    /**
     * Maps a UserStrategyEntity to UserStrategyDto.
     *
     * @param entity User strategy entity
     * @return User strategy DTO
     */
    public UserStrategyDto toUserStrategyDto(UserStrategyEntity entity) {
        if (entity == null) {
            return null;
        }

        return new UserStrategyDto(
                entity.getId(),
                entity.getName(),
                baseStrategyMapper.toBaseStrategyDto(entity.getBaseStrategy()),
                entity.getSymbol(),
                entity.getStatus().name(),
                entity.getBudget(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Maps a list of UserStrategyEntity to list of UserStrategyDto.
     *
     * @param entities List of user strategy entities
     * @return List of user strategy DTOs
     */
    public List<UserStrategyDto> toUserStrategyDtoList(List<UserStrategyEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toUserStrategyDto)
                .toList();
    }
}



