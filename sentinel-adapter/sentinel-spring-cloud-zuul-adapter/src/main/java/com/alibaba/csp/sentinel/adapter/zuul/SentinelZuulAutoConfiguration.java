package com.alibaba.csp.sentinel.adapter.zuul;

import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelErrorFilter;
import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelPostFilter;
import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelPreFilter;
import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.netflix.zuul.ZuulFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties.PREFIX;


/**
 * @author tiger
 */
@Configuration
@EnableConfigurationProperties(SentinelZuulProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class SentinelZuulAutoConfiguration {

    private final SentinelZuulProperties sentinelZuulProperties;

    public SentinelZuulAutoConfiguration(SentinelZuulProperties sentinelZuulProperties) {
        this.sentinelZuulProperties = sentinelZuulProperties;
    }

    @Bean
    public ZuulFilter preFilter() {
        return new SentinelPreFilter(sentinelZuulProperties);
    }

    @Bean
    public ZuulFilter postFilter() {
        return new SentinelPostFilter(sentinelZuulProperties);
    }

    @Bean
    public ZuulFilter errorFilter() {
        return new SentinelErrorFilter(sentinelZuulProperties);
    }

}
