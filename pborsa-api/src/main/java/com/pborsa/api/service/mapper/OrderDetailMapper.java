package com.pborsa.api.service.mapper;

import com.pborsa.api.domain.dto.strategy.FilledOrdersResponse;
import com.pborsa.api.domain.dto.strategy.OrderDetailDto;
import com.pborsa.api.domain.entity.OrderEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting OrderEntity to OrderDetailDto.
 * Single responsibility: Maps OrderEntity ↔ OrderDetailDto only.
 */
@Component
public class OrderDetailMapper {

    /**
     * Maps an OrderEntity to OrderDetailDto.
     *
     * @param entity Order entity
     * @return Order detail DTO
     */
    public OrderDetailDto toOrderDetailDto(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        Long userStrategyId = entity.getUserStrategy() != null ? entity.getUserStrategy().getId() : null;

        return new OrderDetailDto(
                entity.getId(),
                entity.getAlpacaOrderId(),
                entity.getClientOrderId(),
                entity.getSymbol(),
                entity.getQuantity(),
                entity.getFilledQuantity(),
                entity.getSide(),
                entity.getType(),
                entity.getTimeInForce(),
                entity.getLimitPrice(),
                entity.getStopPrice(),
                null, // filledAveragePrice - not stored in OrderEntity
                entity.getStatus(),
                null, // message - not stored in OrderEntity
                entity.getExtendedHours(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getSubmittedAt(),
                entity.getFilledAt(),
                entity.getExpiredAt(),
                entity.getCancelledAt(),
                null, // assetClass - not stored in OrderEntity
                userStrategyId
        );
    }

    /**
     * Maps a list of OrderEntity to list of OrderDetailDto.
     *
     * @param entities List of order entities
     * @return List of order detail DTOs
     */
    public List<OrderDetailDto> toOrderDetailDtoList(List<OrderEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toOrderDetailDto)
                .toList();
    }

    public List<OrderDetailDto> toFilledOrders(List<OrderEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toOrderDetailDto)
                .toList();
    }

    public FilledOrdersResponse toFilledOrderResponse(OrderEntity order){
        if(order == null){
            return null;
        }

        return new FilledOrdersResponse(
                order.getSymbol(),
                order.getQuantity(),
                order.getSide(),
                order.getUpdatedAt()
        );
    }


}


