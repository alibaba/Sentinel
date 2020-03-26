package com.alibaba.csp.sentinel.dashboard.rule.type.nacos;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author cdfive
 */
@Configuration
@PropertySource(value = {"classpath:nacos.properties", "file:nacos.properties"}, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "nacos")
public class NacosProperties {

    private String serverAddr;

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }
}
