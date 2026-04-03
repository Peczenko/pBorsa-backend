package com.pborsa.api.tradingengine;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.pborsa.domain.dto.market.StockBarDto;
import com.pborsa.api.tradingengine.v1.Bar;
import com.pborsa.api.tradingengine.v1.LiveBarUpdate;
import com.pborsa.api.tradingengine.v1.LiveBarUpdateAck;
import com.pborsa.api.tradingengine.v1.SymbolBar;
import com.pborsa.api.tradingengine.v1.TradingEngineServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * gRPC implementation of LiveBarClient.
 * Uses blocking stub for simple request-response pattern.
 */
@Slf4j
public class GrpcLiveBarClient implements LiveBarClient, AutoCloseable {

    private static final int CALL_TIMEOUT_SECONDS = 10;
    private static final int CHANNEL_SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final ManagedChannel channel;
    private final TradingEngineServiceGrpc.TradingEngineServiceBlockingStub blockingStub;

    public GrpcLiveBarClient(String targetAddress) {
        this(ManagedChannelBuilder.forTarget(targetAddress)
                .usePlaintext()
                .build());
    }

    public GrpcLiveBarClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = TradingEngineServiceGrpc.newBlockingStub(channel)
                .withDeadlineAfter(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public LiveBarResult sendLiveBars(Map<String, StockBarDto> bars, String timeframe, List<Long> strategyIds) {
        if (bars == null || bars.isEmpty()) {
            return LiveBarResult.success(0);
        }

        try {
            LiveBarUpdate.Builder updateBuilder = LiveBarUpdate.newBuilder()
                    .setUpdateTime(toTimestamp(Instant.now()));

            for (Map.Entry<String, StockBarDto> entry : bars.entrySet()) {
                String symbol = entry.getKey();
                StockBarDto barDto = entry.getValue();

                SymbolBar symbolBar = SymbolBar.newBuilder()
                        .setSymbol(symbol)
                        .setTimeframe(timeframe)
                        .setBar(toProtoBar(barDto))
                        .build();

                updateBuilder.addBars(symbolBar);
            }

            if (strategyIds != null) {
                updateBuilder.addAllStrategyIds(strategyIds);
            }

            LiveBarUpdate request = updateBuilder.build();
            LiveBarUpdateAck response = blockingStub
                    .withDeadlineAfter(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .sendLiveBars(request);

            if (response.getReceived()) {
                log.debug("Live bars sent successfully: {} bars, message={}",
                        response.getBarsProcessed(), response.getMessage());
                return LiveBarResult.success(response.getBarsProcessed());
            } else {
                log.warn("Live bars rejected by trading engine: {}", response.getMessage());
                return LiveBarResult.failure(response.getMessage());
            }

        } catch (StatusRuntimeException e) {
            log.error("gRPC error sending live bars: {}", e.getStatus(), e);
            return LiveBarResult.failure("gRPC error: " + e.getStatus().getDescription());
        } catch (Exception e) {
            log.error("Error sending live bars", e);
            return LiveBarResult.failure("Error: " + e.getMessage());
        }
    }

    private static Bar toProtoBar(StockBarDto bar) {
        Bar.Builder builder = Bar.newBuilder()
                .setTimestamp(toTimestamp(bar.timestamp()));
        if (bar.open() != null) {
            builder.setOpen(bar.open().doubleValue());
        }
        if (bar.high() != null) {
            builder.setHigh(bar.high().doubleValue());
        }
        if (bar.low() != null) {
            builder.setLow(bar.low().doubleValue());
        }
        if (bar.close() != null) {
            builder.setClose(bar.close().doubleValue());
        }
        if (bar.volume() != null) {
            builder.setVolume(bar.volume());
        }
        if (bar.tradeCount() != null) {
            builder.setTradeCount(bar.tradeCount());
        }
        if (bar.vwap() != null) {
            builder.setVwap(bar.vwap().doubleValue());
        }
        return builder.build();
    }

    private static Timestamp toTimestamp(Instant instant) {
        if (instant == null) {
            return Timestamps.fromMillis(0);
        }
        return Timestamps.fromMillis(instant.toEpochMilli());
    }

    @Override
    public void close() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            try {
                channel.awaitTermination(CHANNEL_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
