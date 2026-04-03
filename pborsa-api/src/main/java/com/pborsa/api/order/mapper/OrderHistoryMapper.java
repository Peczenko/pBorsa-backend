package com.pborsa.api.order.mapper;

import com.pborsa.domain.dto.strategy.OrderHistoryDto;
import com.pborsa.api.order.entity.OrderHistoryEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting OrderHistoryEntity to OrderHistoryDto.
 * Single responsibility: Maps OrderHistoryEntity ↔ OrderHistoryDto only.
 */
@Component
public class OrderHistoryMapper {

    /**
     * Maps an OrderHistoryEntity to OrderHistoryDto.
     *
     * @param entity Order history entity
     * @return Order history DTO
     */
    public OrderHistoryDto toOrderHistoryDto(OrderHistoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return new OrderHistoryDto(
                entity.getId(),
                entity.getOrderId(),
                entity.getStatus(),
                entity.getReason(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }

    /**
     * Maps a list of OrderHistoryEntity to list of OrderHistoryDto.
     *
     * @param entities List of order history entities
     * @return List of order history DTOs
     */
    public List<OrderHistoryDto> toOrderHistoryDtoList(List<OrderHistoryEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toOrderHistoryDto)
                .toList();
    }
}


