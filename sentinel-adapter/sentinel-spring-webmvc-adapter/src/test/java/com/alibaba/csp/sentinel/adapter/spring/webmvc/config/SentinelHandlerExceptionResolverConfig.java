package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelHandlerExceptionResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Tank
 * @since 1.8.2
 */
@Configuration
public class SentinelHandlerExceptionResolverConfig {

    @Bean
    public SentinelHandlerExceptionResolver sentinelHandlerExceptionResolver() {
        return new SentinelHandlerExceptionResolver();
    }
}
