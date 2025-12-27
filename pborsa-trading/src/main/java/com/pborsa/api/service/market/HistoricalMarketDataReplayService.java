package com.pborsa.api.service.market;

import com.pborsa.api.domain.dto.market.HistoricalReplayRequest;
import com.pborsa.api.domain.dto.market.HistoricalReplayTickDto;
import com.pborsa.api.domain.dto.market.StockBarDto;
import com.pborsa.api.exception.AlpacaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    private final Map<String, ReplaySession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionReplayIds = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public String startReplay(String sessionId,
                              Long userId,
                              HistoricalReplayRequest request,
                              String workflowId,
                              Consumer<String> workflowStopper,
                              BiConsumer<String, HistoricalReplayTickDto> consumer) {
        validateRequest(request);

        String symbol = request.symbol().toUpperCase();
        Instant start = request.start();
        Instant end = request.end();
        int stepSeconds = request.stepSeconds() != null ? request.stepSeconds() : DEFAULT_STEP_SECONDS;
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
        ReplaySession session = new ReplaySession(sessionId, userId, symbol, start, end, stepSeconds, bars, consumer);
        session.workflowId = workflowId;
        session.workflowStopper = workflowStopper;
        sessions.put(replayId, session);
        if (sessionId != null) {
            sessionReplayIds.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet()).add(replayId);
        }

        scheduleReplay(replayId);

        log.info("Started historical replay {} for user {} symbol {}", replayId, userId, symbol);
        return replayId;
    }

    public Optional<ReplayStopContext> stopReplay(String replayId) {
        ReplaySession session = sessions.remove(replayId);
        if (session == null) {
            log.info("Historical replay {} not found", replayId);
            return Optional.empty();
        }

        session.running = false;
        cancelScheduledTask(session);
        unlinkFromSession(session.sessionId, replayId);
        stopWorkflowIfAttached(session, replayId);

        log.info("Stopped historical replay {}", replayId);
        return Optional.of(new ReplayStopContext(replayId, session.userId, session.workflowId));
    }

    public List<ReplayStopContext> stopAllForSession(String sessionId) {
        Set<String> replayIds = sessionReplayIds.remove(sessionId);
        if (replayIds == null || replayIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReplayStopContext> stopped = new ArrayList<>();
        for (String replayId : replayIds) {
            stopReplay(replayId).ifPresent(stopped::add);
        }
        return stopped;
    }

    private void emitTick(String replayId) {
        ReplaySession session = sessions.get(replayId);
        if (session == null || !session.running) {
            return;
        }

        boolean finished = false;
        StockBarDto latestBar = null;

        synchronized (session) {
            if (!session.running) {
                return;
            }

            session.playbackTime = session.playbackTime.plusSeconds(session.stepSeconds);
            if (session.barIndex >= session.bars.size() || session.playbackTime.isAfter(session.end)) {
                finished = true;
                session.running = false;
            } else {
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
            }
        }

        if (finished) {
            stopReplay(replayId);
            return;
        }

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

    private void scheduleReplay(String replayId) {
        ReplaySession session = sessions.get(replayId);
        if (session == null) {
            return;
        }

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> emitTick(replayId),
                0,
                DEFAULT_TICK_MILLIS,
                TimeUnit.MILLISECONDS
        );
        session.scheduledTask = future;
    }

    private void cancelScheduledTask(ReplaySession session) {
        if (session.scheduledTask != null) {
            session.scheduledTask.cancel(false);
        }
    }

    private void unlinkFromSession(String sessionId, String replayId) {
        if (sessionId == null) {
            return;
        }
        Set<String> replayIds = sessionReplayIds.get(sessionId);
        if (replayIds != null) {
            replayIds.remove(replayId);
            if (replayIds.isEmpty()) {
                sessionReplayIds.remove(sessionId);
            }
        }
    }

    private void stopWorkflowIfAttached(ReplaySession session, String replayId) {
        if (session.workflowId != null && session.workflowStopper != null) {
            try {
                session.workflowStopper.accept(session.workflowId);
            } catch (Exception e) {
                log.warn("Failed to stop workflow {} for replay {}", session.workflowId, replayId, e);
            }
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

    private String generateReplayId(Long userId, String symbol) {
        return "replay-%s-%s-%s".formatted(userId, symbol, UUID.randomUUID());
    }

    public record ReplayStopContext(String replayId, Long userId, String workflowId) {}


    private static class ReplaySession {
        private final String sessionId;
        private final Long userId;
        private final String symbol;
        private final Instant start;
        private final Instant end;
        private final int stepSeconds;
        private final List<StockBarDto> bars;
        private final BiConsumer<String, HistoricalReplayTickDto> consumer;
        private Instant playbackTime;
        private int barIndex;
        private long sequence;
        private volatile boolean running;
        private String workflowId;
        private Consumer<String> workflowStopper;
        private ScheduledFuture<?> scheduledTask;

        private ReplaySession(String sessionId,
                              Long userId,
                              String symbol,
                              Instant start,
                              Instant end,
                              int stepSeconds,
                              List<StockBarDto> bars,
                              BiConsumer<String, HistoricalReplayTickDto> consumer) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.symbol = symbol;
            this.start = start;
            this.end = end;
            this.stepSeconds = stepSeconds;
            this.bars = bars;
            this.consumer = consumer;
            this.playbackTime = start;
            this.running = true;
        }
    }
}
