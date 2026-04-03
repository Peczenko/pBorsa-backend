package com.pborsa.trading.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for high-performance caching using Caffeine.
 * Defines multiple cache instances with different TTL and size configurations.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    public static final String API_CREDENTIALS_CACHE = "apiCredentials";
    public static final String ALPACA_CLIENT_CACHE = "alpacaClients";
    public static final String MARKET_DATA_CACHE = "marketData";
    public static final String POSITIONS_CACHE = "positions";
    public static final String ACCOUNT_INFO_CACHE = "accountInfo";
    public static final String ORDERS_CACHE = "orders";

    @Value("${cache.api-credentials.maximum-size:1000}")
    private int apiCredentialsMaxSize;

    @Value("${cache.api-credentials.expire-after-write:1h}")
    private Duration apiCredentialsExpiry;

    @Value("${cache.market-data.maximum-size:10000}")
    private int marketDataMaxSize;

    @Value("${cache.market-data.expire-after-write:5s}")
    private Duration marketDataExpiry;

    @Value("${cache.positions.maximum-size:5000}")
    private int positionsMaxSize;

    @Value("${cache.positions.expire-after-write:30s}")
    private Duration positionsExpiry;

    @Value("${cache.account-info.maximum-size:1000}")
    private int accountInfoMaxSize;

    @Value("${cache.account-info.expire-after-write:1m}")
    private Duration accountInfoExpiry;

    /**
     * Primary cache manager with default settings.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCacheBuilder());
        return cacheManager;
    }

    /**
     * Specialized cache manager for API credentials (long TTL).
     */
    @Bean("apiCredentialsCacheManager")
    public CacheManager apiCredentialsCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(API_CREDENTIALS_CACHE, ALPACA_CLIENT_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(apiCredentialsMaxSize)
                .expireAfterWrite(apiCredentialsExpiry)
                .recordStats());
        return cacheManager;
    }

    /**
     * Specialized cache manager for market data (short TTL for freshness).
     */
    @Bean("marketDataCacheManager")
    public CacheManager marketDataCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(MARKET_DATA_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(marketDataMaxSize)
                .expireAfterWrite(marketDataExpiry)
                .recordStats());
        return cacheManager;
    }

    /**
     * Specialized cache manager for positions.
     */
    @Bean("positionsCacheManager")
    public CacheManager positionsCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(POSITIONS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(positionsMaxSize)
                .expireAfterWrite(positionsExpiry)
                .recordStats());
        return cacheManager;
    }

    /**
     * Specialized cache manager for account info.
     */
    @Bean("accountInfoCacheManager")
    public CacheManager accountInfoCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(ACCOUNT_INFO_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(accountInfoMaxSize)
                .expireAfterWrite(accountInfoExpiry)
                .recordStats());
        return cacheManager;
    }

    /**
     * Default cache builder with reasonable defaults.
     */
    private Caffeine<Object, Object> defaultCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats();
    }
}

