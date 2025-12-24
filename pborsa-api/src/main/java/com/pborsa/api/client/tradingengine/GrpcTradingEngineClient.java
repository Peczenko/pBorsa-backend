package com.pborsa.api.client.tradingengine;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.tradingengine.v1.Bar;
import com.pborsa.api.tradingengine.v1.BarBatch;
import com.pborsa.api.tradingengine.v1.ExecutionAck;
import com.pborsa.api.tradingengine.v1.StrategyExecutionChunk;
import com.pborsa.api.tradingengine.v1.StrategyExecutionHeader;
import com.pborsa.api.tradingengine.v1.TradingEngineServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * gRPC implementation of TradingEngineClient.
 */
@Slf4j
public class GrpcTradingEngineClient implements TradingEngineClient, AutoCloseable {

    private final ManagedChannel channel;
    private final TradingEngineServiceGrpc.TradingEngineServiceStub asyncStub;

    public GrpcTradingEngineClient(String targetAddress) {
        this(ManagedChannelBuilder.forTarget(targetAddress)
                .usePlaintext()
                .build());
    }

    public GrpcTradingEngineClient(ManagedChannel channel) {
        this.channel = channel;
        this.asyncStub = TradingEngineServiceGrpc.newStub(channel);
    }

    @Override
    public TradingEngineStream startExecution(StrategyExecutionContext context) {
        Objects.requireNonNull(context, "Strategy execution context is required");

        AtomicReference<ExecutionAck> ackRef = new AtomicReference<>();
        CountDownLatch finished = new CountDownLatch(1);

        StreamObserver<ExecutionAck> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ExecutionAck value) {
                ackRef.set(value);
                log.info("Trading engine ack for execution {}: accepted={}, message={}",
                        context.executionId(), value.getAccepted(), value.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                log.error("Trading engine stream error for execution {}", context.executionId(), t);
                finished.countDown();
            }

            @Override
            public void onCompleted() {
                finished.countDown();
            }
        };

        StreamObserver<StrategyExecutionChunk> requestObserver = asyncStub.executeStrategy(responseObserver);
        requestObserver.onNext(StrategyExecutionChunk.newBuilder()
                .setHeader(toHeader(context))
                .build());

        return new TradingEngineStream() {
            @Override
            public void sendBatch(List<StockBarDto> bars) {
                if (bars == null || bars.isEmpty()) {
                    return;
                }
                BarBatch.Builder batchBuilder = BarBatch.newBuilder();
                for (StockBarDto bar : bars) {
                    batchBuilder.addBars(toProtoBar(bar));
                }
                requestObserver.onNext(StrategyExecutionChunk.newBuilder()
                        .setBatch(batchBuilder.build())
                        .build());
            }

            @Override
            public void closeStream() {
                try {
                    requestObserver.onCompleted();
                    finished.await(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("Error closing trading engine stream for execution {}", context.executionId(), e);
                }
            }
        };
    }

    private StrategyExecutionHeader toHeader(StrategyExecutionContext context) {
        return StrategyExecutionHeader.newBuilder()
                .setExecutionId(context.executionId())
                .setUserId(context.userId())
                .setStrategyId(context.strategyId())
                .setSymbol(context.symbol())
                .setTimeframe(context.timeframe())
                .setStart(toTimestamp(context.start()))
                .setEnd(toTimestamp(context.end()))
                .build();
    }

    private Bar toProtoBar(StockBarDto bar) {
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

    private Timestamp toTimestamp(java.time.Instant instant) {
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
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
