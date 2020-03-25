package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author cdfive
 */
@Configuration
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "apollo")
@EnableConfigurationProperties(ApolloProperties.class)
public class ApolloConfig {

}
