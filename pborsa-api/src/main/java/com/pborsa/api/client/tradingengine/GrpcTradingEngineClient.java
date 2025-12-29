package com.pborsa.api.client.tradingengine;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.pborsa.api.domain.dto.market.StockTradeDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.tradingengine.v1.Trade;
import com.pborsa.api.tradingengine.v1.TradeBatch;
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
        AtomicReference<Throwable> streamError = new AtomicReference<>();
        CountDownLatch finished = new CountDownLatch(1);

        StreamObserver<ExecutionAck> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ExecutionAck value) {
                ackRef.set(value);
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

        StreamObserver<StrategyExecutionChunk> requestObserver = asyncStub.executeStrategy(responseObserver);
        requestObserver.onNext(StrategyExecutionChunk.newBuilder()
                .setHeader(toHeader(context))
                .build());

        return new TradingEngineStream() {
            @Override
            public void sendTrades(List<StockTradeDto> trades) {
                ensureHealthy();
                if (trades == null || trades.isEmpty()) {
                    return;
                }
                TradeBatch.Builder batchBuilder = TradeBatch.newBuilder();
                for (StockTradeDto trade : trades) {
                    batchBuilder.addTrades(toProtoTrade(trade));
                }
                requestObserver.onNext(StrategyExecutionChunk.newBuilder()
                        .setTradeBatch(batchBuilder.build())
                        .build());
            }

            @Override
            public void ensureHealthy() {
                Throwable error = streamError.get();
                if (error != null) {
                    throw new IllegalStateException("Trading engine stream failed for execution "
                            + context.executionId(), error);
                }
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
                .setStart(toTimestamp(context.start()))
                .setEnd(toTimestamp(context.end()))
                .build();
    }

    private Trade toProtoTrade(StockTradeDto trade) {
        Trade.Builder builder = Trade.newBuilder()
                .setTimestamp(toTimestamp(trade.timestamp()));
        if (trade.price() != null) {
            builder.setPrice(trade.price().doubleValue());
        }
        if (trade.size() != null) {
            builder.setSize(trade.size().doubleValue());
        }
        if (trade.exchange() != null) {
            builder.setExchange(trade.exchange());
        }
        if (trade.tradeId() != null) {
            builder.setTradeId(trade.tradeId());
        }
        if (trade.tape() != null) {
            builder.setTape(trade.tape());
        }
        if (trade.conditions() != null) {
            builder.setConditions(trade.conditions());
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
