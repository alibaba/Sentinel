/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository.redis;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * @author cdfive
 */
@Configuration
@ConditionalOnClass(RedisClient.class)
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "redis")
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);

    @Autowired
    private RedisProperties redisProperties;

    @PostConstruct
    public void init() {
        redisProperties.logInfo();
    }

    public RedisClient createRedisClient() {
        String host = redisProperties.getHost();
        AssertUtil.assertNotBlank(host, "RedisClient init failed, host can't be blank");

        Integer port = redisProperties.getPort();
        AssertUtil.assertState(port != null, "RedisClient init failed, port can't null");

        RedisURI.Builder redisURIBuilder = RedisURI.builder();
        redisURIBuilder.withHost(host)
                       .withPort(port);

        String password = redisProperties.getPassword();
        if (StringUtil.isNotBlank(password)) {
            redisURIBuilder.withPassword(password);
        }

        Integer database = redisProperties.getDatabase();
        if (database != null) {
            redisURIBuilder.withDatabase(database);
        }

        Long timeoutMs = redisProperties.getTimeoutMs();
        if (timeoutMs != null) {
            redisURIBuilder.withTimeout(Duration.ofMillis(timeoutMs));
        }

        try {
            RedisClient redisClient = RedisClient.create(redisURIBuilder.build());
            return redisClient;
        } catch (Throwable e) {
            LOGGER.info("CreateRedisClient error", e);
            throw e;
        }
    }
}
