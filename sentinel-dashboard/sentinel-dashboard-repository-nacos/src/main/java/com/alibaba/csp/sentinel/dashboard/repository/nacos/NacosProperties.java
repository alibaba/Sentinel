package com.alibaba.csp.sentinel.dashboard.repository.nacos;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author cdfive
 */
@Configuration
@PropertySource(value = {"classpath:repository/nacos.properties", "file:nacos.properties"}, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "nacos")
public class NacosProperties {

    private String serverAddr;

    private String sentinelGroup;

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getSentinelGroup() {
        return sentinelGroup;
    }

    public void setSentinelGroup(String sentinelGroup) {
        this.sentinelGroup = sentinelGroup;
    }
}
