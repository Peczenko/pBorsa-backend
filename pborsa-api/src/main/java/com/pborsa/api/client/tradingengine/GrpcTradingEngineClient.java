package com.pborsa.api.client.tradingengine;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.tradingengine.v1.Quote;
import com.pborsa.api.tradingengine.v1.QuoteBatch;
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
            public void sendQuotes(List<StockQuoteDto> quotes) {
                ensureHealthy();
                if (quotes == null || quotes.isEmpty()) {
                    return;
                }
                QuoteBatch.Builder batchBuilder = QuoteBatch.newBuilder();
                for (StockQuoteDto quote : quotes) {
                    batchBuilder.addQuotes(toProtoQuote(quote));
                }
                requestObserver.onNext(StrategyExecutionChunk.newBuilder()
                        .setQuoteBatch(batchBuilder.build())
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

    private Quote toProtoQuote(StockQuoteDto quote) {
        Quote.Builder builder = Quote.newBuilder()
                .setTimestamp(toTimestamp(quote.timestamp()));
        if (quote.bidPrice() != null) {
            builder.setBidPrice(quote.bidPrice().doubleValue());
        }
        if (quote.bidSize() != null) {
            builder.setBidSize(quote.bidSize().doubleValue());
        }
        if (quote.askPrice() != null) {
            builder.setAskPrice(quote.askPrice().doubleValue());
        }
        if (quote.askSize() != null) {
            builder.setAskSize(quote.askSize().doubleValue());
        }
        if (quote.bidExchange() != null) {
            builder.setBidExchange(quote.bidExchange());
        }
        if (quote.askExchange() != null) {
            builder.setAskExchange(quote.askExchange());
        }
        if (quote.tape() != null) {
            builder.setTape(quote.tape());
        }
        if (quote.conditions() != null) {
            builder.setConditions(quote.conditions());
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
