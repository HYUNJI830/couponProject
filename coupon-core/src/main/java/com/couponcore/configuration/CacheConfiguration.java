package com.couponcore.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;


@RequiredArgsConstructor
@Configuration
public class CacheConfiguration {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    @Primary
    public CacheManager redisCacheManager() {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                //캐시 키를 직렬화
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                //캐시 값을 직렬화
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                //캐시 항목 생존시간 설정
                .entryTtl(Duration.ofMinutes(30)); //30분
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory)
                //기본 캐시 설정 지정
                .cacheDefaults(redisCacheConfiguration)
                //인스턴스 생성
                .build();
    }
}
