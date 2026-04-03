package com.pborsa.api.backtest.mapper;

import com.pborsa.api.strategy.mapper.BaseStrategyMapper;
import com.pborsa.domain.dto.backtest.BacktestDto;
import com.pborsa.domain.dto.backtest.BacktestOrderDto;
import com.pborsa.domain.dto.backtest.BacktestSummaryDto;
import com.pborsa.api.backtest.entity.BacktestEntity;
import com.pborsa.api.backtest.entity.BacktestOrderEntity;
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
     * Maps a BacktestEntity to BacktestDto with order counts.
     *
     * @param entity Backtest entity
     * @return Backtest DTO
     */
    public BacktestDto toBacktestDto(BacktestEntity entity) {
        return toBacktestDto(entity, 0, 0);
    }

    /**
     * Maps a BacktestEntity to BacktestSummaryDto with order counts.
     *
     * @param entity Backtest entity
     * @return Backtest summary DTO
     */
    public BacktestSummaryDto toBacktestSummaryDto(BacktestEntity entity) {
        return toBacktestSummaryDto(entity, 0, 0);
    }

    /**
     * Maps a BacktestEntity to BacktestSummaryDto with order counts.
     *
     * @param entity         Backtest entity
     * @param buyOrdersCount Total number of BUY orders
     * @param sellOrdersCount Total number of SELL orders
     * @return Backtest summary DTO
     */
    public BacktestSummaryDto toBacktestSummaryDto(BacktestEntity entity, int buyOrdersCount, int sellOrdersCount) {
        if (entity == null) {
            return null;
        }

        return new BacktestSummaryDto(
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
                buyOrdersCount,
                sellOrdersCount,
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt()
        );
    }

    /**
     * Maps a BacktestEntity to BacktestDto with order counts.
     *
     * @param entity Backtest entity
     * @param buyOrdersCount Total number of BUY orders
     * @param sellOrdersCount Total number of SELL orders
     * @return Backtest DTO
     */
    public BacktestDto toBacktestDto(BacktestEntity entity, int buyOrdersCount, int sellOrdersCount) {
        if (entity == null) {
            return null;
        }

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
                buyOrdersCount,
                sellOrdersCount,
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt()
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

    /**
     * Maps a list of BacktestEntity to list of BacktestSummaryDto.
     *
     * @param entities List of backtest entities
     * @return List of backtest summary DTOs
     */
    public List<BacktestSummaryDto> toBacktestSummaryDtoList(List<BacktestEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toBacktestSummaryDto)
                .toList();
    }
}
