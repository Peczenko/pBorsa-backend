package com.pborsa.api.client.tradingengine;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.pborsa.api.domain.dto.backtest.BacktestExecutionContext;
import com.pborsa.api.domain.dto.backtest.BacktestOrderDto;
import com.pborsa.api.domain.dto.backtest.BacktestResultDto;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.tradingengine.v1.BacktestOrder;
import com.pborsa.api.tradingengine.v1.BacktestRequest;
import com.pborsa.api.tradingengine.v1.BacktestResponse;
import com.pborsa.api.tradingengine.v1.Bar;
import com.pborsa.api.tradingengine.v1.TradingEngineServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * gRPC implementation of BacktestClient.
 */
@Slf4j
public class GrpcBacktestClient implements BacktestClient, AutoCloseable {

    private static final int BACKTEST_TIMEOUT_MINUTES = 120;
    private static final int CHANNEL_SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final ManagedChannel channel;
    private final TradingEngineServiceGrpc.TradingEngineServiceBlockingStub blockingStub;

    public GrpcBacktestClient(String targetAddress) {
        this(ManagedChannelBuilder.forTarget(targetAddress)
                .usePlaintext()
                .build());
    }

    public GrpcBacktestClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = TradingEngineServiceGrpc.newBlockingStub(channel)
                .withDeadlineAfter(BACKTEST_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public BacktestResultDto executeBacktest(
            BacktestExecutionContext context,
            List<StockBarDto> historyBeforeStart,
            List<StockBarDto> historyTestingRange
    ) {
        Objects.requireNonNull(context, "Backtest execution context is required");
        Objects.requireNonNull(historyTestingRange, "Testing range history is required");

        log.info("Executing backtest {} for strategy {} on symbol {}",
                context.backtestId(), context.baseStrategyCode(), context.symbol());

        BacktestRequest request = buildRequest(context, historyBeforeStart, historyTestingRange);
        BacktestResponse response = blockingStub
                .withDeadlineAfter(BACKTEST_TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .executeBacktest(request);

        log.info("Backtest {} completed: success={}, totalTrades={}, pnl={}",
                context.backtestId(), response.getSuccess(), response.getTotalTrades(), response.getPnl());

        return toResultDto(context.backtestId(), response);
    }

    private BacktestRequest buildRequest(
            BacktestExecutionContext context,
            List<StockBarDto> historyBeforeStart,
            List<StockBarDto> historyTestingRange
    ) {
        BacktestRequest.Builder builder = BacktestRequest.newBuilder()
                .setBacktestId(context.backtestId().toString())
                .setUserId(context.userId())
                .setSymbol(context.symbol())
                .setBaseStrategyCode(context.baseStrategyCode())
                .setTestingStart(toTimestamp(context.testingStart()))
                .setTestingEnd(toTimestamp(context.testingEnd()))
                .setBudget(context.budget().doubleValue());

        if (historyBeforeStart != null) {
            for (StockBarDto bar : historyBeforeStart) {
                builder.addHistoryBeforeStart(toProtoBar(bar));
            }
        }

        for (StockBarDto bar : historyTestingRange) {
            builder.addHistoryTestingRange(toProtoBar(bar));
        }

        return builder.build();
    }

    private BacktestResultDto toResultDto(Long backtestId, BacktestResponse response) {
        List<BacktestOrderDto> orders = response.getOrdersList().stream()
                .map(this::toOrderDto)
                .toList();

        return BacktestResultDto.builder()
                .backtestId(backtestId)
                .success(response.getSuccess())
                .message(response.getMessage())
                .orders(orders)
                .pnl(BigDecimal.valueOf(response.getPnl()))
                .maxDrawdown(BigDecimal.valueOf(response.getMaxDrawdown()))
                .totalTrades(response.getTotalTrades())
                .winningTrades(response.getWinningTrades())
                .build();
    }

    private BacktestOrderDto toOrderDto(BacktestOrder order) {
        return new BacktestOrderDto(
                null, // ID will be assigned when persisted
                order.getSymbol(),
                order.getSide().name(),
                BigDecimal.valueOf(order.getQuantity()),
                BigDecimal.valueOf(order.getPrice()),
                Instant.ofEpochMilli(Timestamps.toMillis(order.getExecutedAt())),
                null // createdAt will be set when persisted
        );
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
