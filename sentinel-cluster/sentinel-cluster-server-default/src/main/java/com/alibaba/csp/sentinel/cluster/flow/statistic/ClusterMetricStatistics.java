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
package com.alibaba.csp.sentinel.cluster.flow.statistic;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterMetric;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterMetricStatistics {

    private static final Map<Long, ClusterMetric> METRIC_MAP = new ConcurrentHashMap<>();

    public static void clear() {
        METRIC_MAP.clear();
    }

    public static void putMetric(long id, ClusterMetric metric) {
        AssertUtil.notNull(metric, "Cluster metric cannot be null");
        METRIC_MAP.put(id, metric);
    }

    public static boolean putMetricIfAbsent(long id, ClusterMetric metric) {
        AssertUtil.notNull(metric, "Cluster metric cannot be null");
        if (METRIC_MAP.containsKey(id)) {
            return false;
        }
        METRIC_MAP.put(id, metric);
        return true;
    }

    public static void removeMetric(long id) {
        METRIC_MAP.remove(id);
    }

    public static ClusterMetric getMetric(long id) {
        return METRIC_MAP.get(id);
    }

    public static void resetFlowMetrics() {
        Set<Long> keySet = METRIC_MAP.keySet();
        for (Long id : keySet) {
            METRIC_MAP.put(id, new ClusterMetric(ClusterServerConfigManager.getSampleCount(),
                ClusterServerConfigManager.getIntervalMs()));
        }
    }

    private ClusterMetricStatistics() {}
}
