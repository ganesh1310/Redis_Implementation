package com.example.Redis.Config;

import java.time.Duration;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory factory) {

        RedisCacheConfiguration defaults =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new GenericJackson2JsonRedisSerializer()));

            return RedisCacheManager.builder(factory)
                    .cacheDefaults(defaults)
                    .withInitialCacheConfigurations(
                            Map.of(
                                    "products",
                                    defaults.entryTtl(Duration.ofMinutes(10)),
                                    "productLists",
                                    defaults.entryTtl(Duration.ofMinutes(2)),
                                    "customers",
                                    defaults.entryTtl(Duration.ofMinutes(5)),
                                    "orders",
                                    defaults.entryTtl(Duration.ofMinutes(15))
                                    // these are caches across different entities, each with their own TTL (time to live) settings
                            )
                    )
                    .build();
    }
}