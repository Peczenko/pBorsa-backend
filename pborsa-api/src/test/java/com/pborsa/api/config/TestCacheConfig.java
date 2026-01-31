package com.pborsa.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Minimal test configuration that provides a no-op CacheManager.
 * <p>
 * Use this for tests that don't need full security configuration
 * (like @DataJpaTest) but still need a CacheManager due to @EnableCaching.
 */
@TestConfiguration
@Profile("test")
public class TestCacheConfig {

    /**
     * Creates a no-op CacheManager for testing.
     * This satisfies the @EnableCaching requirement without actual caching.
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}
