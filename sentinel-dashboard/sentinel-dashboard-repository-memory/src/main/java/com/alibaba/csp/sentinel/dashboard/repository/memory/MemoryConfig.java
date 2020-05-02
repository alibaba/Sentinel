package com.alibaba.csp.sentinel.dashboard.repository.memory;

import com.alibaba.csp.sentinel.transport.client.SentinelTransportClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cdfive
 */
@Configuration
//@ConditionalOnMissingBean(NacosConfig.class)
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "memory", matchIfMissing = true)
public class MemoryConfig {

    public MemoryConfig() {
        System.out.println("MemoryConfig init");
    }

    @Bean
    public SentinelTransportClient sentinelTransportClient() {
        return new SentinelTransportClient();
    }
}
