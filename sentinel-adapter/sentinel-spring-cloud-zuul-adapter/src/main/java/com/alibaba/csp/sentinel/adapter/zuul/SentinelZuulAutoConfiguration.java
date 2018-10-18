/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.csp.sentinel.adapter.zuul;

import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelErrorFilter;
import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelPostFilter;
import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelPreFilter;
import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.netflix.zuul.ZuulFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties.PREFIX;

/**
 * Sentinel Spring Cloud Zuul AutoConfiguration
 *
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
    @ConditionalOnMissingBean(ProxyRequestHelper.class)
    public ProxyRequestHelper proxyRequestHelper() {
        return new ProxyRequestHelper();
    }

    @Bean
    public ZuulFilter preFilter( ProxyRequestHelper proxyRequestHelper) {
        return new SentinelPreFilter(sentinelZuulProperties, proxyRequestHelper, new SentinelPreFilter.MockTestService());
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
