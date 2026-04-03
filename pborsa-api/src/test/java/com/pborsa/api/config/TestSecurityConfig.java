package com.pborsa.api.config;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Test security configuration that disables authentication for API testing.
 * <p>
 * This configuration:
 * - Permits all requests without authentication
 * - Provides a mock FirebaseAuth bean
 * - Provides a no-op CacheManager for tests
 * - Disables CSRF protection
 * - Uses stateless session management
 */
@TestConfiguration
@Profile("test")
@EnableWebSecurity
public class TestSecurityConfig {

    /**
     * Creates a permissive security filter chain for testing.
     * All requests are permitted without authentication.
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Creates a mock FirebaseAuth bean for testing.
     * This prevents the real FirebaseAuth from being initialized.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public FirebaseAuth firebaseAuth() {
        return mock(FirebaseAuth.class);
    }

    /**
     * Creates a no-op CacheManager for testing.
     * This satisfies the @EnableCaching requirement without actual caching.
     * Only used when no other CacheManager is defined (e.g., in @WebMvcTest).
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager testCacheManager() {
        return new NoOpCacheManager();
    }

    /**
     * Named cache manager for API credentials - required by services using @Cacheable.
     */
    @Bean("apiCredentialsCacheManager")
    @ConditionalOnMissingBean(name = "apiCredentialsCacheManager")
    public CacheManager apiCredentialsCacheManager() {
        return new NoOpCacheManager();
    }

    /**
     * Named cache manager for market data - required by services using @Cacheable.
     */
    @Bean("marketDataCacheManager")
    @ConditionalOnMissingBean(name = "marketDataCacheManager")
    public CacheManager marketDataCacheManager() {
        return new NoOpCacheManager();
    }

    /**
     * Named cache manager for positions - required by services using @Cacheable.
     */
    @Bean("positionsCacheManager")
    @ConditionalOnMissingBean(name = "positionsCacheManager")
    public CacheManager positionsCacheManager() {
        return new NoOpCacheManager();
    }

    /**
     * Named cache manager for account info - required by services using @Cacheable.
     */
    @Bean("accountInfoCacheManager")
    @ConditionalOnMissingBean(name = "accountInfoCacheManager")
    public CacheManager accountInfoCacheManager() {
        return new NoOpCacheManager();
    }
}
