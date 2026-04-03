package com.pborsa.api.order.service;

import com.pborsa.domain.dto.credentials.AlpacaCredentialsDto;
import com.pborsa.domain.exception.CredentialsNotFoundException;
import com.pborsa.trading.alpaca.AlpacaClientFactory;
import com.pborsa.trading.credentials.UserCredentialsService;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate.TradeUpdate;
import net.jacobpeterson.alpaca.model.websocket.updates.model.tradeupdate.TradeUpdateMessage;
import net.jacobpeterson.alpaca.websocket.updates.UpdatesWebsocketInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class TradeUpdatesStreamManager {

    private final AlpacaClientFactory clientFactory;
    private final UserCredentialsService credentialsService;
    private final OrderQueryService orderQueryService;
    private final TradeUpdatesProcessor tradeUpdatesProcessor;
    private final OrderUpdateQueueManager orderUpdateQueueManager;

    @Value("${alpaca.trade-updates.idle-timeout-ms:300000}")
    private long idleTimeoutMs;

    @Value("${alpaca.trade-updates.auth-timeout-seconds:10}")
    private long authTimeoutSeconds;

    private final Map<Long, StreamState> streams = new ConcurrentHashMap<>();

    public TradeUpdatesStreamManager(AlpacaClientFactory clientFactory,
                                     UserCredentialsService credentialsService,
                                     OrderQueryService orderQueryService,
                                     TradeUpdatesProcessor tradeUpdatesProcessor,
                                     OrderUpdateQueueManager orderUpdateQueueManager) {
        this.clientFactory = clientFactory;
        this.credentialsService = credentialsService;
        this.orderQueryService = orderQueryService;
        this.tradeUpdatesProcessor = tradeUpdatesProcessor;
        this.orderUpdateQueueManager = orderUpdateQueueManager;
    }

    /**
     * Ensures a connected trade updates stream exists for the user.
     * If the stream is disconnected, it will be reconnected immediately.
     *
     * @return true if stream is connected and ready, false otherwise
     */
    public boolean ensureStream(Long userId) {
        if (userId == null) {
            log.warn("Skipping trade updates stream for null userId");
            return false;
        }

        StreamState existing = streams.get(userId);

        // If stream exists but is disconnected, reconnect immediately
        if (existing != null) {
            if (existing.stream.isConnected()) {
                return true;
            }
            log.info("Stream exists but disconnected for user {}, reconnecting", userId);
            reconnect(userId, existing);
            return existing.stream.isConnected();
        }

        // No stream exists, create new one
        StreamState newState = startStream(userId);
        if (newState != null) {
            streams.put(userId, newState);
            return true;
        }
        return false;
    }

    public void stopStream(Long userId) {
        StreamState state = streams.remove(userId);
        if (state == null) {
            return;
        }
        try {
            state.stream.disconnect();
            log.info("Trade updates stream disconnected for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to disconnect trade updates stream for user {}", userId, e);
        }
    }

    @Scheduled(fixedDelayString = "${alpaca.trade-updates.sync-interval-ms:60000}")
    public void syncStreams() {
        Set<Long> activeUsers = new HashSet<>(orderQueryService.findUsersWithOpenOrders());
        for (Long userId : activeUsers) {
            ensureStream(userId);
        }

        long now = System.currentTimeMillis();
        for (Map.Entry<Long, StreamState> entry : streams.entrySet()) {
            Long userId = entry.getKey();
            StreamState state = entry.getValue();

            if (!state.stream.isConnected()) {
                reconnect(userId, state);
            }

            if (activeUsers.contains(userId)) {
                continue;
            }

            long idleForMs = now - state.lastActivityMs.get();
            if (idleForMs >= idleTimeoutMs && !orderQueryService.hasOpenOrders(userId)) {
                log.info("Closing trade updates stream for user {} after {}ms idle", userId, idleForMs);
                stopStream(userId);
            }
        }
    }

    private StreamState startStream(Long userId) {
        try {
            AlpacaCredentialsDto credentials = credentialsService.getCredentials(userId);
            AlpacaAPI client = clientFactory.getOrCreateClient(credentials);
            UpdatesWebsocketInterface stream = client.updatesStream();

            StreamState state = new StreamState(stream);
            stream.setListener(message -> handleUpdate(userId, message, state));

            if (!stream.isConnected()) {
                stream.connect();
            }
            if (!stream.waitForAuthorization(authTimeoutSeconds, TimeUnit.SECONDS)) {
                log.warn("Trade updates stream not authorized for user {} within {}s", userId, authTimeoutSeconds);
                stream.disconnect();
                return null;
            }
            stream.subscribeToTradeUpdates(true);
            state.touch();

            log.info("Trade updates stream connected for user {}", userId);
            return state;
        } catch (CredentialsNotFoundException e) {
            log.warn("Skipping trade updates stream, missing credentials for user {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Failed to start trade updates stream for user {}", userId, e);
            return null;
        }
    }

    private void reconnect(Long userId, StreamState state) {
        try {
            state.stream.connect();
            if (!state.stream.waitForAuthorization(authTimeoutSeconds, TimeUnit.SECONDS)) {
                log.warn("Trade updates stream not authorized for user {} within {}s", userId, authTimeoutSeconds);
                return;
            }
            state.stream.subscribeToTradeUpdates(true);
            state.touch();
            log.info("Trade updates stream reconnected for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to reconnect trade updates stream for user {}", userId, e);
        }
    }

    private void handleUpdate(Long userId, TradeUpdateMessage message, StreamState state) {
        state.touch();
        if (message == null || message.getData() == null) {
            log.debug("Trade update message is empty for user {}", userId);
            return;
        }
        TradeUpdate update = message.getData();
        
        // Extract order key for queue routing
        String orderKey = extractOrderKey(update);
        
        // Submit to order-specific queue to ensure sequential processing
        orderUpdateQueueManager.submitUpdate(orderKey, () -> {
            tradeUpdatesProcessor.processUpdate(userId, update);
        });
    }

    /**
     * Extracts order identifier from trade update for queue routing.
     * Prefers alpacaOrderId, falls back to clientOrderId.
     */
    private String extractOrderKey(TradeUpdate update) {
        if (update == null || update.getOrder() == null) {
            return null;
        }
        var order = update.getOrder();
        // Prefer alpacaOrderId as it's more stable
        if (order.getId() != null && !order.getId().isBlank()) {
            return "alpaca:" + order.getId();
        }
        if (order.getClientOrderId() != null && !order.getClientOrderId().isBlank()) {
            return "client:" + order.getClientOrderId();
        }
        return null;
    }

    private static final class StreamState {
        private final UpdatesWebsocketInterface stream;
        private final AtomicLong lastActivityMs = new AtomicLong();

        private StreamState(UpdatesWebsocketInterface stream) {
            this.stream = stream;
            touch();
        }

        private void touch() {
            lastActivityMs.set(Instant.now().toEpochMilli());
        }
    }
}
