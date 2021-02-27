package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.repository.metric.InMemoryMetricsRepository;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link MetricsRepository}'s implementation bean configuration.
 *
 * @author wxq
 */
@Configuration
public class MetricsRepositoryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MetricsRepository<MetricEntity> metricEntityMetricsRepository() {
        return new InMemoryMetricsRepository();
    }

}
