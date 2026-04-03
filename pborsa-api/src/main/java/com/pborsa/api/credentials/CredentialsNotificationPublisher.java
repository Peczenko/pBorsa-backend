package com.pborsa.api.credentials;

import com.pborsa.domain.event.CredentialsChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;

/**
 * Publishes Postgres notifications when credentials change so other apps can evict caches.
 */
// TODO: migrate to credentials versioning to insure against lost notifications
@Component
@ConditionalOnProperty(name = "credentials.notify.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CredentialsNotificationPublisher {

    private final JdbcTemplate jdbcTemplate;
    private final String channel;

    public CredentialsNotificationPublisher(
            JdbcTemplate jdbcTemplate,
            @Value("${credentials.notify.channel:credentials_updated}") String channel) {
        this.jdbcTemplate = jdbcTemplate;
        this.channel = channel;
    }

    @EventListener
    public void onCredentialsChanged(CredentialsChangedEvent event) {
        if (event == null || event.userId() == null) {
            log.warn("Skipping credentials notification with empty userId");
            return;
        }

        try {
            jdbcTemplate.execute((ConnectionCallback<Object>) connection -> {
                try (PreparedStatement statement = connection.prepareStatement("select pg_notify(?, ?)")) {
                    statement.setString(1, channel);
                    statement.setString(2, String.valueOf(event.userId()));
                    statement.execute();
                }
                return null;
            });
            log.info("Published credentials update notification for user {} (active={})", event.userId(), event.active());
        } catch (Exception e) {
            log.error("Failed to publish credentials update notification for user {}", event.userId(), e);
        }
    }
}
