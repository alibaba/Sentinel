package com.alibaba.csp.sentinel.dashboard.rule.type.apollo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author cdfive
 */
@Configuration
@PropertySource(value = {"classpath:apollo.properties", "file:apollo.properties"}, ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "apollo")
public class ApolloProperties {

    private String portalUrl;

    private String token;

    public String getPortalUrl() {
        return portalUrl;
    }

    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
