package com.alibaba.csp.sentinel.dashboard.support.redis;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
public class SpringRedisTemplateBuilder {

    public static <T> RedisTemplate<String, T> build(final RedisConnectionFactory factory, Class<T> type) {
        final FastJsonRedisSerializer<T> valueSerializer = new FastJsonRedisSerializer<>(type);

        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setDefaultSerializer(RedisSerializer.string());

        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(valueSerializer);

        redisTemplate.setStringSerializer(RedisSerializer.string());

        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(valueSerializer);

        return redisTemplate;
    }

}