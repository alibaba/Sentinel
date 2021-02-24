/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository.metric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author luoxy
 * @since 2020-06-15 10:03/1.0
 */
@Component
public class MetricsRepositoryHolder {
    @Value("${sentinel.metric.repository: lowMemory}")
    private String repositoryType;

    @Autowired
    private Map<String, MetricsRepository> metricsRepositories;

    public MetricsRepository get() {
        MetricsRepository repository = metricsRepositories.get(repositoryType);
        if (repository == null) {
            throw new RuntimeException("no find a MetricsRepository with bean name " + repositoryType);
        }
        return repository;
    }
}
