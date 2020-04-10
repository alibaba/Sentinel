package com.alibaba.csp.sentinel.dashboard.repository.memory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
}
