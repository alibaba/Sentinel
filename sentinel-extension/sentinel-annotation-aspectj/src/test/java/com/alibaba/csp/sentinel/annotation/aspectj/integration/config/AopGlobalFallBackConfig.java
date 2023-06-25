package com.alibaba.csp.sentinel.annotation.aspectj.integration.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.annotation.aspectj.integration.fallback.AnnotationGlobalFallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author luffy
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan("com.alibaba.csp.sentinel.annotation.aspectj.integration")

public class AopGlobalFallBackConfig {
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect(new AnnotationGlobalFallback());
    }
}
