/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.mybatis.config;

import com.alibaba.csp.sentinel.adapter.mybatis.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author kaizi2009
 */
@Configuration
public class InterceptorConfig {

    @Bean
    @Order(5)
    public SentinelTotalInterceptor newTotalInterceptor() {
        return new SentinelTotalInterceptor();
    }

    @Bean
    @Order(4)
    public SentinelReadInterceptor newReadInterceptor() {
        return new SentinelReadInterceptor();
    }

    @Bean
    @Order(3)
    public SentinelWriteInterceptor newWriteInterceptor() {
        return new SentinelWriteInterceptor();
    }

    @Bean
    @Order(2)
    public SentinelCommandTypeInterceptor newSentinelCommandTypeInterceptor() {
        return new SentinelCommandTypeInterceptor();
    }

    @Bean
    @Order(1)
    public SentinelMapperInterceptor newSentinelInterceptor() {
        return new SentinelMapperInterceptor();
    }

    @Bean
    @Order(0)
    public SentinelSqlInterceptor newSentinelSqlInterceptor() {
        return new SentinelSqlInterceptor();
    }

}
