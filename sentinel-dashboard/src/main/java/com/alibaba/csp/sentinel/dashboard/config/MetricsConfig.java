package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.client.InfluxDbUtils;
import com.alibaba.csp.sentinel.dashboard.repository.metric.InfluxDBMetricsRepository;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.influx")
public class MetricsConfig {
    public String database;
    private String retentionPolicy;

    @Bean
    public InfluxDbUtils influxDbUtils(){
        return new InfluxDbUtils(database);
    }

    @Bean
    @ConditionalOnMissingBean(name = "influxDb")
    public MetricsRepository metricStore(){
        return new InfluxDBMetricsRepository();
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setRetentionPolicy(String retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }
}
