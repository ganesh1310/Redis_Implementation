package com.example.Redis.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisCacheErrorHandler {

    private static final Logger log =
            LoggerFactory.getLogger(RedisCacheErrorHandler.class);

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(
                    RuntimeException ex, Cache cache, Object key) {
                log.error("Redis cache GET failed. Cache: {}, key: {}",
                        cache.getName(), key, ex);
                // Suppress the cache error.
                // Spring proceeds as a cache miss.
            }

            @Override
            public void handleCachePutError(
                    RuntimeException ex, Cache cache, Object key,
                    Object value) {
                log.error("Redis cache PUT failed. Cache: {}, key: {}",
                        cache.getName(), key, ex);
                // Database operation can still succeed.
            }

            @Override
            public void handleCacheEvictError(
                    RuntimeException ex, Cache cache, Object key) {
                log.error("Redis cache EVICT failed. Cache: {}, key: {}",
                        cache.getName(), key, ex);
            }

            @Override
            public void handleCacheClearError(
                    RuntimeException ex, Cache cache) {
                log.error("Redis cache CLEAR failed. Cache: {}",
                        cache.getName(), ex);
            }
        };
    }
}
