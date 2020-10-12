package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.client.InfluxDbUtils;
import com.alibaba.csp.sentinel.dashboard.repository.metric.InMemoryMetricsRepository;
import com.alibaba.csp.sentinel.dashboard.repository.metric.InfluxDBMetricsRepository;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Configuration(proxyBeanMethods = false)
    @ConfigurationProperties(prefix = "spring.influx")
    protected static class MetricsConfiguration {
        public String database;
        private String retentionPolicy;

        @Bean
        @ConditionalOnProperty(value = "spring.influx.enabled",matchIfMissing = true)
        public InfluxDbUtils influxDbUtils(){
            return new InfluxDbUtils(database);
        }

        @Bean
        @ConditionalOnProperty(value = "spring.influx.enabled",matchIfMissing = true)
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

    @Configuration(proxyBeanMethods = false)
    protected static class EmbeddedMetricsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name={"metricStore"})
        public MetricsRepository metricStore(){
            return new InMemoryMetricsRepository();
        }
    }
}
