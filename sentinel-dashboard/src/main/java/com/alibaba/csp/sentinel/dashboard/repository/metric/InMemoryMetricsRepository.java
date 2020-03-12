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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

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
    private Map<String, Map<String, LinkedHashMap<Long, MetricEntity>>> allMetrics = new ConcurrentHashMap<>();


    private Map<String, ReadWriteLock> appLockMap = new ConcurrentHashMap(); //lock seperate by app


    @Override
    public void save(MetricEntity entity, String app) {
        allMetrics.get(app).computeIfAbsent(entity.getResource(), e -> new LinkedHashMap<Long, MetricEntity>(100) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, MetricEntity> eldest) {
                return eldest.getKey() < System.currentTimeMillis() - MAX_METRIC_LIVE_TIME_MS;
            }
        }).put(entity.getTimestamp().getTime(), entity);
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics, String app) {

        if (metrics == null) {
            return;
        }

        if (appLockMap.get(app) == null) { //DCL to the same app concurrent request

            synchronized (this) {
                if (appLockMap.get(app) == null) {
                    appLockMap.put(app, new ReentrantReadWriteLock());
                    allMetrics.put(app, new HashMap<>(256));
                    lockAndWriteData(appLockMap.get(app), app, metrics);
                } else { //
                    lockAndWriteData(appLockMap.get(app), app, metrics);
                }
            }

        } else {
            lockAndWriteData(appLockMap.get(app), app, metrics);
        }


    }


    /**
     * seperate lock by app to enhance write performance
     *
     * @param readWriteLock
     * @param metrics
     */
    private void lockAndWriteData(ReadWriteLock readWriteLock, String app, Iterable<MetricEntity> metrics) {
        try {
            readWriteLock.writeLock().lock();
            metrics.forEach(entity -> save(entity, app));
        } finally {
            readWriteLock.writeLock().unlock(); //release write lock
        }
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource,
                                                           long startTime, long endTime) {
        if (appLockMap.get(app) == null) {
            return new ArrayList<>(0);
        }
        List<MetricEntity> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        Map<String, LinkedHashMap<Long, MetricEntity>> resourceMap = allMetrics.get(app);
        if (resourceMap == null) {
            return results;
        }
        LinkedHashMap<Long, MetricEntity> metricsMap = resourceMap.get(resource);
        if (metricsMap == null) {
            return results;
        }
        appLockMap.get(app).readLock().lock();
        try {
            for (Entry<Long, MetricEntity> entry : metricsMap.entrySet()) {
                if (entry.getKey() >= startTime && entry.getKey() <= endTime) {
                    results.add(entry.getValue());
                }
            }
            return results;
        } finally {
            appLockMap.get(app).readLock().unlock();
        }
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        if (appLockMap.get(app) == null) {
            return new ArrayList<>(0);
        }
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        // resource -> timestamp -> metric
        Map<String, LinkedHashMap<Long, MetricEntity>> resourceMap = allMetrics.get(app);
        if (resourceMap == null) {
            return results;
        }
        final long minTimeMs = System.currentTimeMillis() - 1000 * 60;
        Map<String, MetricEntity> resourceCount = new ConcurrentHashMap<>(32);

        appLockMap.get(app).readLock().lock();
        try {
            for (Entry<String, LinkedHashMap<Long, MetricEntity>> resourceMetrics : resourceMap.entrySet()) {
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
        } finally {
            appLockMap.get(app).readLock().unlock();
        }
    }
}
