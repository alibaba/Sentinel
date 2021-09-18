package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelHandlerExceptionResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Copyright (c) 2021年09月18日 4:32 下午
 * <p>
 * Company: 苏州渠成易销网络科技有限公司
 * <p>
 *
 * @author shuiping.zhu@successchannel.com
 * @version 1.0.0
 */
@Configuration
public class SentinelHandlerExceptionResolverConfig {

    @Bean
    public SentinelHandlerExceptionResolver sentinelHandlerExceptionResolver() {
        return new SentinelHandlerExceptionResolver();
    }
}
