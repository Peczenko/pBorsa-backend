package com.pborsa.api.service.mapper;

import com.google.protobuf.Timestamp;
import com.pborsa.api.domain.entity.UserStrategyStatus;
import com.pborsa.api.domain.event.StrategyStatusChangedEvent;
import com.pborsa.api.tradingengine.v1.StrategyStatus;
import com.pborsa.api.tradingengine.v1.StrategyStatusNotification;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper for converting domain strategy status events to gRPC protobuf messages.
 */
@Component
public class StrategyStatusProtoMapper {

    /**
     * Converts a StrategyStatusChangedEvent to StrategyStatusNotification protobuf message.
     */
    public StrategyStatusNotification toProto(StrategyStatusChangedEvent event) {
        if (event == null) {
            return null;
        }

        return StrategyStatusNotification.newBuilder()
                .setStrategyId(event.strategyId())
                .setUserId(event.userId())
                .setSymbol(event.symbol())
                .setOldStatus(mapStrategyStatus(event.oldStatus()))
                .setNewStatus(mapStrategyStatus(event.newStatus()))
                .setUpdatedAt(toTimestamp(Instant.now()))
                .build();
    }

    private StrategyStatus mapStrategyStatus(UserStrategyStatus status) {
        if (status == null) {
            return StrategyStatus.STRATEGY_STATUS_UNSPECIFIED;
        }
        return switch (status) {
            case CREATED -> StrategyStatus.STRATEGY_CREATED;
            case PREPARING -> StrategyStatus.STRATEGY_PREPARING;
            case ACTIVE -> StrategyStatus.STRATEGY_ACTIVE;
            case PAUSED -> StrategyStatus.STRATEGY_PAUSED;
            case STOPPED -> StrategyStatus.STRATEGY_STOPPED;
            case START_FAILED -> StrategyStatus.STRATEGY_START_FAILED;
        };
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
