package com.alibaba.csp.sentinel.dashboard.rule.type.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author cdfive
 */
@Configuration
//@ConditionalOnMissingBean(NacosConfig.class)
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "api", matchIfMissing = true)
public class ApiConfig {

    public ApiConfig() {
        System.out.println("ApiConfig init");
    }
}
