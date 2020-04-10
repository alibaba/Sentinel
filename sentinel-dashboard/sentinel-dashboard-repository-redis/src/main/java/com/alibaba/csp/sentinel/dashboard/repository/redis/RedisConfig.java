package com.alibaba.csp.sentinel.dashboard.repository.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author cdfive
 */
@Configuration
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "redis")
public class RedisConfig {


}
