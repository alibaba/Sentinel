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
package com.alibaba.csp.sentinel.dashboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelApolloPrivateConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String appId;

    private final String env;

    private final String clusterName;

    public SentinelApolloPrivateConfiguration(
            @Value("${app.id}") String appId,
            @Value("${env}") String env,
            @Value("${apollo.cluster}") String clusterName) {
        this.appId = appId;
        this.env = env;
        this.clusterName = clusterName;
    }

    public String getAppId() {
        return appId;
    }

    public String getEnv() {
        return env;
    }

    public String getClusterName() {
        return clusterName;
    }
}
