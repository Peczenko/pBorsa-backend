    package com.pborsa.api.service.credentials;

    import com.pborsa.api.exception.CredentialsNotFoundException;
    import com.pborsa.api.service.alpaca.AlpacaClientFactory;
    import lombok.extern.slf4j.Slf4j;
    import org.postgresql.PGConnection;
    import org.postgresql.PGNotification;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
    import org.springframework.stereotype.Component;

    import jakarta.annotation.PostConstruct;
    import jakarta.annotation.PreDestroy;
    import javax.sql.DataSource;
    import java.sql.Connection;
    import java.sql.Statement;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.concurrent.atomic.AtomicBoolean;

    /**
     * Listens for credential updates via Postgres LISTEN/NOTIFY and refreshes local caches.
     */
    @Component
    @ConditionalOnProperty(name = "credentials.notify.enabled", havingValue = "true", matchIfMissing = true)
    @Slf4j
    public class CredentialsNotificationListener {

        private final DataSource dataSource;
        private final UserCredentialsService credentialsService;
        private final AlpacaClientFactory alpacaClientFactory;
        private final ExecutorService listenerExecutor;
        private final AtomicBoolean running = new AtomicBoolean(true);

        private final String channel;
        private final long pollIntervalMs;
        private volatile Connection activeConnection;

        public CredentialsNotificationListener(
                DataSource dataSource,
                UserCredentialsService credentialsService,
                AlpacaClientFactory alpacaClientFactory,
                @Value("${credentials.notify.channel:credentials_updated}") String channel,
                @Value("${credentials.notify.poll-interval-ms:1000}") long pollIntervalMs) {
            this.dataSource = dataSource;
            this.credentialsService = credentialsService;
            this.alpacaClientFactory = alpacaClientFactory;
            this.channel = channel;
            this.pollIntervalMs = pollIntervalMs;
            this.listenerExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "credentials-notify-listener"));
        }

        @PostConstruct
        public void start() {
            listenerExecutor.execute(this::listenLoop);
        }

        @PreDestroy
        public void stop() {
            running.set(false);
            closeConnection();
            listenerExecutor.shutdownNow();
        }

        private void listenLoop() {
            while (running.get()) {
                try (Connection connection = dataSource.getConnection();
                     Statement statement = connection.createStatement()) {
                    activeConnection = connection;
                    connection.setAutoCommit(true);
                    statement.execute("LISTEN " + channel);
                    log.info("Listening for credentials updates on channel {}", channel);

                    PGConnection pgConnection = connection.unwrap(PGConnection.class);
                    while (running.get()) {
                        PGNotification[] notifications = pgConnection.getNotifications();
                        if (notifications != null) {
                            for (PGNotification notification : notifications) {
                                handleNotification(notification.getParameter());
                            }
                        }
                        Thread.sleep(pollIntervalMs);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running.set(false);
                } catch (Exception e) {
                    log.error("Credentials notify listener failed, retrying in {} ms", pollIntervalMs, e);
                    sleepQuietly(pollIntervalMs);
                } finally {
                    closeConnection();
                }
            }
        }

    private void handleNotification(String payload) {
        String trimmed = payload == null ? "" : payload.trim();
        if (trimmed.isEmpty()) {
            log.warn("Received credentials update notification with empty userId");
            return;
        }

        Long userId;
        try {
            userId = Long.valueOf(trimmed);
        } catch (NumberFormatException e) {
            log.warn("Received credentials update notification with invalid userId payload {}", trimmed, e);
            return;
        }

        try {
            alpacaClientFactory.evictClient(userId);
            credentialsService.refreshCredentials(userId);
            log.info("Refreshed credentials cache for user {}", userId);
        } catch (CredentialsNotFoundException e) {
            alpacaClientFactory.evictClient(userId);
            log.info("Credentials missing for user {}, evicted caches", userId);
        } catch (Exception e) {
            log.error("Failed to refresh credentials cache for user {}", userId, e);
        }
    }

        private void closeConnection() {
            if (activeConnection != null) {
                try {
                    activeConnection.close();
                } catch (Exception e) {
                    log.debug("Failed to close credentials listener connection", e);
                } finally {
                    activeConnection = null;
                }
            }
        }

        private void sleepQuietly(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                running.set(false);
            }
        }
    }
