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
package com.alibaba.csp.sentinel.datasource.spring.cloud.config.config;

import com.alibaba.csp.sentinel.datasource.spring.cloud.config.SentinelRuleLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * <p>
 * Define the configuration Loaded when spring application start.
 * Put it in META-INF/spring.factories, it will be auto loaded by Spring
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
@Configuration
public class DataSourceBootstrapConfiguration {

    @Autowired
    private ConfigurableEnvironment environment;

    @Bean
    public SentinelRuleLocator sentinelPropertySourceLocator(ConfigClientProperties properties) {
        SentinelRuleLocator locator = new SentinelRuleLocator(
                properties, environment);
        return locator;
    }


}
