/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.spring.webflux.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Eric Zhao
 */
@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory){
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
            .<String, String>newSerializationContext(new StringRedisSerializer())
            .hashKey(new StringRedisSerializer())
            .hashValue(new StringRedisSerializer())
            .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
