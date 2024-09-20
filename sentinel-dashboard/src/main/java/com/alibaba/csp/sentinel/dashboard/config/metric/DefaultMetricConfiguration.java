package com.alibaba.csp.sentinel.dashboard.config.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.repository.metric.InMemoryMetricsRepository;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
@Configuration
@ConditionalOnProperty(prefix = "metric", name = "store-type", havingValue = "default", matchIfMissing = true)
public class DefaultMetricConfiguration {


    @Bean
    public MetricsRepository<MetricEntity> metricStore() {
        return new InMemoryMetricsRepository();
    }

}
