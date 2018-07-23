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
package com.taobao.csp.sentinel.dashboard.inmem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.taobao.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import org.springframework.stereotype.Component;

/**
 * Store metrics in memory.
 *
 * @author leyou
 */
@Component
public class InMemMetricStore {
    public static final long MAX_METRIC_LIVE_TIME_MS = 1000 * 60 * 5;
    /**
     * {@code app -> resource -> timestamp -> metric}
     */
    private Map<String, Map<String, LinkedHashMap<Long, MetricEntity>>> allMetrics = new ConcurrentHashMap<>();

    /**
     * Save all metrics in memory. Metric older than {@link #MAX_METRIC_LIVE_TIME_MS} will be removed.
     *
     * @param metrics metrics to be saved.
     */
    public synchronized void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        for (MetricEntity entity : metrics) {
            allMetrics.computeIfAbsent(entity.getApp(), e -> new HashMap<>(16))
                .computeIfAbsent(entity.getResource(), e -> new LinkedHashMap<Long, MetricEntity>() {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Long, MetricEntity> eldest) {
                        return eldest.getKey() < System.currentTimeMillis() - MAX_METRIC_LIVE_TIME_MS;
                    }
                }).put(entity.getTimestamp().getTime(), entity);
        }
    }

    public synchronized List<MetricEntity> queryByAppAndResouce(String app,
                                                                String resource,
                                                                long startTime,
                                                                long endTime) {
        List<MetricEntity> results = new ArrayList<>();
        Map<String, LinkedHashMap<Long, MetricEntity>> resouceMap = allMetrics.get(app);
        if (resouceMap == null) {
            return results;
        }
        LinkedHashMap<Long, MetricEntity> metricsMap = resouceMap.get(resource);
        if (metricsMap == null) {
            return results;
        }
        for (Map.Entry<Long, MetricEntity> entry : metricsMap.entrySet()) {
            if (entry.getKey() >= startTime && entry.getKey() <= endTime) {
                results.add(entry.getValue());
            }
        }
        return results;
    }

    /**
     * Find resources of App order by last minute b_qps desc
     *
     * @param app app name
     * @return Resources list, order by last minute b_qps desc.
     */
    public synchronized List<String> findResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        // resource -> timestamp -> metric
        Map<String, LinkedHashMap<Long, MetricEntity>> resourceMap = allMetrics.get(app);
        if (resourceMap == null) {
            return results;
        }
        final long minTimeMs = System.currentTimeMillis() - 1000 * 60;
        Map<String, MetricEntity> resourceCount = new HashMap<>(32);

        for (Map.Entry<String, LinkedHashMap<Long, MetricEntity>> resourceMetrics : resourceMap.entrySet()) {
            for (Map.Entry<Long, MetricEntity> metrics : resourceMetrics.getValue().entrySet()) {
                if (metrics.getKey() < minTimeMs) {
                    continue;
                }
                MetricEntity newEntity = metrics.getValue();
                if (resourceCount.containsKey(resourceMetrics.getKey())) {
                    MetricEntity oldEntity = resourceCount.get(resourceMetrics.getKey());
                    oldEntity.addPassedQps(newEntity.getPassedQps());
                    oldEntity.addRtAndSuccessQps(newEntity.getRt(), newEntity.getSuccessQps());
                    oldEntity.addBlockedQps(newEntity.getBlockedQps());
                    oldEntity.addException(newEntity.getException());
                    oldEntity.addCount(1);
                } else {
                    resourceCount.put(resourceMetrics.getKey(), MetricEntity.copyOf(newEntity));
                }
            }
        }
        return resourceCount.entrySet().stream().sorted((o1, o2) -> {
            MetricEntity e1 = o1.getValue();
            MetricEntity e2 = o2.getValue();
            int t = e2.getBlockedQps().compareTo(e1.getBlockedQps());
            if (t != 0) {
                return t;
            }
            return e2.getPassedQps().compareTo(e1.getPassedQps());
        }).map(e -> e.getKey())
            .collect(Collectors.toList());
    }

}
