package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.client.DefaultSentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelApiClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SentinelApiClient sentinelApiClient() {
        return new DefaultSentinelApiClient();
    }

}
