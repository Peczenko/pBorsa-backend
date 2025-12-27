package com.pborsa.api.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class FirebaseConfiguration {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.credentials.base64:}")
    private String credentialsBase64;

    @Value("${firebase.project-id:}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            GoogleCredentials credentials = loadCredentials();
            FirebaseOptions.Builder options = FirebaseOptions.builder()
                    .setCredentials(credentials);

            if (projectId != null && !projectId.isBlank()) {
                options.setProjectId(projectId);
            }

            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing FirebaseApp for project {}", projectId == null ? "" : projectId);
                return FirebaseApp.initializeApp(options.build());
            }

            return FirebaseApp.getInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize FirebaseApp", e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    private GoogleCredentials loadCredentials() throws Exception {
        if (credentialsBase64 != null && !credentialsBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(credentialsBase64.getBytes(StandardCharsets.UTF_8));
            try (InputStream stream = new ByteArrayInputStream(decoded)) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        if (credentialsPath != null && !credentialsPath.isBlank()) {
            try (InputStream stream = new FileInputStream(credentialsPath)) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        log.warn("Firebase credentials not provided, using application default credentials");
        return GoogleCredentials.getApplicationDefault();
    }
}
