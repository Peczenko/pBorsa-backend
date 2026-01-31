package com.pborsa.api.service.mapper;

import com.pborsa.api.domain.dto.backtest.BacktestDto;
import com.pborsa.api.domain.dto.backtest.BacktestOrderDto;
import com.pborsa.api.domain.entity.BacktestEntity;
import com.pborsa.api.domain.entity.BacktestOrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting backtest entities to DTOs.
 */
@Component
@RequiredArgsConstructor
public class BacktestMapper {

    private final BaseStrategyMapper baseStrategyMapper;

    /**
     * Maps a BacktestEntity to BacktestDto without orders.
     *
     * @param entity Backtest entity
     * @return Backtest DTO
     */
    public BacktestDto toBacktestDto(BacktestEntity entity) {
        return toBacktestDto(entity, null);
    }

    /**
     * Maps a BacktestEntity to BacktestDto with orders.
     *
     * @param entity Backtest entity
     * @param orders List of order entities (can be null)
     * @return Backtest DTO
     */
    public BacktestDto toBacktestDto(BacktestEntity entity, List<BacktestOrderEntity> orders) {
        if (entity == null) {
            return null;
        }

        List<BacktestOrderDto> orderDtos = orders != null
                ? orders.stream().map(this::toBacktestOrderDto).toList()
                : null;

        return new BacktestDto(
                entity.getId(),
                entity.getName(),
                baseStrategyMapper.toBaseStrategyDto(entity.getBaseStrategy()),
                entity.getSymbol(),
                entity.getBudget(),
                entity.getTestingStart(),
                entity.getTestingEnd(),
                entity.getStatus().name(),
                entity.getPnl(),
                entity.getMaxDrawdown(),
                entity.getTotalTrades(),
                entity.getWinningTrades(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt(),
                orderDtos
        );
    }

    /**
     * Maps a BacktestOrderEntity to BacktestOrderDto.
     *
     * @param entity Backtest order entity
     * @return Backtest order DTO
     */
    public BacktestOrderDto toBacktestOrderDto(BacktestOrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new BacktestOrderDto(
                entity.getId(),
                entity.getSymbol(),
                entity.getSide(),
                entity.getQuantity(),
                entity.getPrice(),
                entity.getExecutedAt(),
                entity.getCreatedAt()
        );
    }

    /**
     * Maps a list of BacktestEntity to list of BacktestDto.
     *
     * @param entities List of backtest entities
     * @return List of backtest DTOs
     */
    public List<BacktestDto> toBacktestDtoList(List<BacktestEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toBacktestDto)
                .toList();
    }
}
