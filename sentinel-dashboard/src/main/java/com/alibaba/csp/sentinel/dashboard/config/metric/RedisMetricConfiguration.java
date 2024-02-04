package com.alibaba.csp.sentinel.dashboard.config.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import com.alibaba.csp.sentinel.dashboard.repository.metric.RedisMetricsRepository;
import com.alibaba.csp.sentinel.dashboard.support.redis.SpringRedisTemplateBuilder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "metric", name = "store-type", havingValue = "redis")
public class RedisMetricConfiguration {

    @Bean
    public RedisTemplate<String, MetricEntity> metricRedisTemplate(final RedisConnectionFactory factory) {
        return SpringRedisTemplateBuilder.build(factory, MetricEntity.class);
    }

    @Bean
    public MetricsRepository<MetricEntity> metricStore() {
        return new RedisMetricsRepository();
    }

}
