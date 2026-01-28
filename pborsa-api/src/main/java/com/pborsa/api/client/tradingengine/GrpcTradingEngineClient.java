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

    private static final int STREAM_CLOSE_TIMEOUT_SECONDS = 5;
    private static final int CHANNEL_SHUTDOWN_TIMEOUT_SECONDS = 5;

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

        AtomicReference<Throwable> streamError = new AtomicReference<>();
        CountDownLatch finished = new CountDownLatch(1);

        StreamObserver<ExecutionAck> responseObserver = createResponseObserver(context, streamError, finished);
        StreamObserver<StrategyExecutionChunk> requestObserver = asyncStub.executeStrategy(responseObserver);

        sendHeader(requestObserver, context);

        return new GrpcTradingEngineStream(context.executionId(), requestObserver, streamError, finished);
    }

    private StreamObserver<ExecutionAck> createResponseObserver(
            StrategyExecutionContext context,
            AtomicReference<Throwable> streamError,
            CountDownLatch finished
    ) {
        return new StreamObserver<>() {
            @Override
            public void onNext(ExecutionAck value) {
                log.info("Trading engine ack for execution {}: accepted={}, message={}",
                        context.executionId(), value.getAccepted(), value.getMessage());
                if (!value.getAccepted()) {
                    streamError.compareAndSet(null,
                            new IllegalStateException("Trading engine rejected execution: " + value.getMessage()));
                }
            }

            @Override
            public void onError(Throwable t) {
                streamError.compareAndSet(null, t);
                log.error("Trading engine stream error for execution {}", context.executionId(), t);
                finished.countDown();
            }

            @Override
            public void onCompleted() {
                finished.countDown();
            }
        };
    }

    private void sendHeader(StreamObserver<StrategyExecutionChunk> requestObserver, StrategyExecutionContext context) {
        requestObserver.onNext(StrategyExecutionChunk.newBuilder()
                .setHeader(toHeader(context))
                .build());
    }

    private StrategyExecutionHeader toHeader(StrategyExecutionContext context) {
        return StrategyExecutionHeader.newBuilder()
                .setExecutionId(context.executionId())
                .setUserId(context.userId())
                .setStrategyId(context.strategyId())
                .setSymbol(context.symbol())
                .setStart(toTimestamp(context.start()))
                .setEnd(toTimestamp(context.end()))
                .build();
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

    private static Timestamp toTimestamp(java.time.Instant instant) {
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

    /**
     * Inner class for the trading engine stream, improving encapsulation.
     */
    private static class GrpcTradingEngineStream implements TradingEngineStream {
        private final String executionId;
        private final StreamObserver<StrategyExecutionChunk> requestObserver;
        private final AtomicReference<Throwable> streamError;
        private final CountDownLatch finished;

        GrpcTradingEngineStream(
                String executionId,
                StreamObserver<StrategyExecutionChunk> requestObserver,
                AtomicReference<Throwable> streamError,
                CountDownLatch finished
        ) {
            this.executionId = executionId;
            this.requestObserver = requestObserver;
            this.streamError = streamError;
            this.finished = finished;
        }

        @Override
        public void sendBars(List<StockBarDto> bars) {
            ensureHealthy();
            if (bars == null || bars.isEmpty()) {
                return;
            }
            BarBatch.Builder batchBuilder = BarBatch.newBuilder();
            for (StockBarDto bar : bars) {
                batchBuilder.addBars(toProtoBar(bar));
            }
            requestObserver.onNext(StrategyExecutionChunk.newBuilder()
                    .setBarBatch(batchBuilder.build())
                    .build());
        }

        @Override
        public void ensureHealthy() {
            Throwable error = streamError.get();
            if (error != null) {
                throw new IllegalStateException("Trading engine stream failed for execution " + executionId, error);
            }
        }

        @Override
        public void closeStream() {
            try {
                requestObserver.onCompleted();
                finished.await(STREAM_CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Error closing trading engine stream for execution {}", executionId, e);
            }
        }
    }
}
