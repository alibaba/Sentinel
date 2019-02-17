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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.springframework.stereotype.Component;

/**
 * Caches metrics data in a period of time in memory.
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
@Component
public class InMemoryMetricsRepository implements MetricsRepository<MetricEntity> {

    private static final long MAX_METRIC_LIVE_TIME_MS = 1000 * 60 * 5;

    /**
     * {@code app -> resource -> timestamp -> metric}
     */
    private Map<String, Map<String, ConcurrentLinkedHashMap<Long, MetricEntity>>> allMetrics = new ConcurrentHashMap<>();



    @Override
    public synchronized void save(MetricEntity entity) {
        if (entity == null || StringUtil.isBlank(entity.getApp())) {
            return;
        }
        allMetrics.computeIfAbsent(entity.getApp(), e -> new ConcurrentHashMap<>(16))
            .computeIfAbsent(entity.getResource(), e -> new ConcurrentLinkedHashMap.Builder<Long, MetricEntity>()
                .maximumWeightedCapacity(MAX_METRIC_LIVE_TIME_MS).weigher((key, value) -> {
                    // Metric older than {@link #MAX_METRIC_LIVE_TIME_MS} will be removed.
                    int weight = (int)(System.currentTimeMillis() - key);
                    // weight must be a number greater than or equal to one
                    return Math.max(weight, 1);
                }).build()).put(entity.getTimestamp().getTime(), entity);
    }

    @Override
    public synchronized void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        metrics.forEach(this::save);
    }

    @Override
    public synchronized List<MetricEntity> queryByAppAndResourceBetween(String app, String resource,
                                                                        long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        Map<String, ConcurrentLinkedHashMap<Long, MetricEntity>> resourceMap = allMetrics.get(app);
        if (resourceMap == null) {
            return results;
        }
        ConcurrentLinkedHashMap<Long, MetricEntity> metricsMap = resourceMap.get(resource);
        if (metricsMap == null) {
            return results;
        }
        for (Entry<Long, MetricEntity> entry : metricsMap.entrySet()) {
            if (entry.getKey() >= startTime && entry.getKey() <= endTime) {
                results.add(entry.getValue());
            }
        }
        return results;
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        // resource -> timestamp -> metric
        Map<String, ConcurrentLinkedHashMap<Long, MetricEntity>> resourceMap = allMetrics.get(app);
        if (resourceMap == null) {
            return results;
        }
        final long minTimeMs = System.currentTimeMillis() - 1000 * 60;
        Map<String, MetricEntity> resourceCount = new ConcurrentHashMap<>(32);

        for (Entry<String, ConcurrentLinkedHashMap<Long, MetricEntity>> resourceMetrics : resourceMap.entrySet()) {
            for (Entry<Long, MetricEntity> metrics : resourceMetrics.getValue().entrySet()) {
                if (metrics.getKey() < minTimeMs) {
                    continue;
                }
                MetricEntity newEntity = metrics.getValue();
                if (resourceCount.containsKey(resourceMetrics.getKey())) {
                    MetricEntity oldEntity = resourceCount.get(resourceMetrics.getKey());
                    oldEntity.addPassQps(newEntity.getPassQps());
                    oldEntity.addRtAndSuccessQps(newEntity.getRt(), newEntity.getSuccessQps());
                    oldEntity.addBlockQps(newEntity.getBlockQps());
                    oldEntity.addExceptionQps(newEntity.getExceptionQps());
                    oldEntity.addCount(1);
                } else {
                    resourceCount.put(resourceMetrics.getKey(), MetricEntity.copyOf(newEntity));
                }
            }
        }
        // Order by last minute b_qps DESC.
        return resourceCount.entrySet()
            .stream()
            .sorted((o1, o2) -> {
                MetricEntity e1 = o1.getValue();
                MetricEntity e2 = o2.getValue();
                int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                if (t != 0) {
                    return t;
                }
                return e2.getPassQps().compareTo(e1.getPassQps());
            })
            .map(Entry::getKey)
            .collect(Collectors.toList());
    }
}
