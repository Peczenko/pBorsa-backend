package com.pborsa.api.service.market;

import com.pborsa.api.domain.dto.market.HistoricalReplayRequest;
import com.pborsa.api.domain.dto.market.HistoricalReplayTickDto;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.exception.AlpacaException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Service that replays historical market data as a real-time stream.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricalMarketDataReplayService {

    private static final int DEFAULT_STEP_SECONDS = 5;
    private static final int DEFAULT_TICK_MILLIS = 50;
    private static final int DEFAULT_TIMEFRAME_MINUTES = 1;
    private static final String DEFAULT_PERIOD = "MINUTE";

    private final MarketDataService marketDataService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ReplaySession> sessions = new ConcurrentHashMap<>();

    public String startReplay(String userId,
                              HistoricalReplayRequest request,
                              BiConsumer<String, HistoricalReplayTickDto> consumer) {
        validateRequest(request);

        String symbol = request.symbol().toUpperCase();
        Instant start = request.start();
        Instant end = request.end();
        int stepSeconds = request.stepSeconds() != null ? request.stepSeconds() : DEFAULT_STEP_SECONDS;
        int tickMillis = request.tickMillis() != null ? request.tickMillis() : DEFAULT_TICK_MILLIS;
        String timeframe = request.timeframe() != null ? request.timeframe() : DEFAULT_PERIOD;

        List<StockBarDto> bars = marketDataService.getHistoricalBars(
                userId,
                symbol,
                DEFAULT_TIMEFRAME_MINUTES,
                timeframe,
                ZonedDateTime.ofInstant(start, ZoneOffset.UTC),
                ZonedDateTime.ofInstant(end, ZoneOffset.UTC),
                null
        );

        if (bars.isEmpty()) {
            throw new AlpacaException(
                    AlpacaException.ErrorCode.MARKET_DATA_ERROR,
                    "No historical bars found for symbol: " + symbol
            );
        }

        bars.sort(Comparator.comparing(StockBarDto::timestamp));

        String replayId = generateReplayId(userId, symbol);
        ReplaySession session = new ReplaySession(userId, symbol, start, end, stepSeconds, tickMillis, bars, consumer);
        sessions.put(replayId, session);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> emitTick(replayId),
                0,
                tickMillis,
                TimeUnit.MILLISECONDS);
        session.future = future;

        log.info("Started historical replay {} for user {} symbol {}", replayId, userId, symbol);
        return replayId;
    }

    public Optional<String> stopReplay(String replayId) {
        ReplaySession session = sessions.remove(replayId);
        if (session == null) {
            return Optional.empty();
        }

        session.running = false;
        if (session.future != null) {
            session.future.cancel(false);
        }

        log.info("Stopped historical replay {}", replayId);
        return Optional.ofNullable(session.workflowId);
    }

    public void attachWorkflow(String replayId, String workflowId) {
        ReplaySession session = sessions.get(replayId);
        if (session != null) {
            session.workflowId = workflowId;
        }
    }

    private void emitTick(String replayId) {
        ReplaySession session = sessions.get(replayId);
        if (session == null || !session.running) {
            return;
        }

        synchronized (session) {
            if (!session.running) {
                return;
            }

            session.playbackTime = session.playbackTime.plusSeconds(session.stepSeconds);
            if (session.playbackTime.isAfter(session.end)) {
                stopReplay(replayId);
                return;
            }

            StockBarDto latestBar = null;
            while (session.barIndex < session.bars.size()) {
                StockBarDto bar = session.bars.get(session.barIndex);
                if (bar.timestamp() == null || bar.timestamp().isAfter(session.playbackTime)) {
                    break;
                }
                latestBar = bar;
                session.barIndex++;
            }

            if (latestBar == null) {
                return;
            }

            session.sequence++;
            HistoricalReplayTickDto tick = HistoricalReplayTickDto.builder()
                    .replayId(replayId)
                    .symbol(session.symbol)
                    .playbackTime(session.playbackTime)
                    .sourceBarTime(latestBar.timestamp())
                    .sequence(session.sequence)
                    .bar(latestBar)
                    .build();
            session.consumer.accept(replayId, tick);
        }
    }

    private void validateRequest(HistoricalReplayRequest request) {
        Objects.requireNonNull(request, "Replay request is required");
        Objects.requireNonNull(request.symbol(), "Symbol is required");
        Objects.requireNonNull(request.start(), "Start time is required");
        Objects.requireNonNull(request.end(), "End time is required");
        if (request.start().isAfter(request.end())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private String generateReplayId(String userId, String symbol) {
        return "replay-%s-%s-%s".formatted(userId, symbol, UUID.randomUUID());
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private static class ReplaySession {
        private final String userId;
        private final String symbol;
        private final Instant start;
        private final Instant end;
        private final int stepSeconds;
        private final int tickMillis;
        private final List<StockBarDto> bars;
        private final BiConsumer<String, HistoricalReplayTickDto> consumer;
        private Instant playbackTime;
        private int barIndex;
        private long sequence;
        private boolean running;
        private ScheduledFuture<?> future;
        private String workflowId;

        private ReplaySession(String userId,
                              String symbol,
                              Instant start,
                              Instant end,
                              int stepSeconds,
                              int tickMillis,
                              List<StockBarDto> bars,
                              BiConsumer<String, HistoricalReplayTickDto> consumer) {
            this.userId = userId;
            this.symbol = symbol;
            this.start = start;
            this.end = end;
            this.stepSeconds = stepSeconds;
            this.tickMillis = tickMillis;
            this.bars = bars;
            this.consumer = consumer;
            this.playbackTime = start;
            this.running = true;
        }
    }
}
