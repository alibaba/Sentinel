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
package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.util.List;

/**
 * Repository interface for aggregated metrics data.
 *
 * @param <T> type of metrics
 * @author Eric Zhao
 */
public interface MetricsRepository<T> {

    /**
     * Save the metric to the storage repository.
     *
     * @param metric metric data to save
     */
    void save(T metric);

    /**
     * Save all metrics to the storage repository.
     *
     * @param metrics metrics to save
     */
    void saveAll(Iterable<T> metrics);

    /**
     * Get all metrics by {@code appName} and {@code resourceName} between a period of time.
     *
     * @param app       application name for Sentinel
     * @param resource  resource name
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return all metrics in query conditions
     */
    List<T> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime);

    /**
     * List resource name of provided application name.
     *
     * @param app application name
     * @return list of resources
     */
    List<String> listResourcesOfApp(String app);
}
