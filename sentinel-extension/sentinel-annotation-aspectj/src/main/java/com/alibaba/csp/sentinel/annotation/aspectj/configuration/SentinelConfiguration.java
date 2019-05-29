package com.alibaba.csp.sentinel.annotation.aspectj.configuration;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelAnnotationBeanProcessor;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelConfiguration {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @Bean
    public SentinelAnnotationBeanProcessor processor() { return new SentinelAnnotationBeanProcessor(); }
}
