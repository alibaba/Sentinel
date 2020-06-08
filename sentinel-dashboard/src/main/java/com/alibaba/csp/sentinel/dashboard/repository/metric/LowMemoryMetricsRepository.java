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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Caches metrics data in a period of time with low memory
 *
 * @author luoxy
 * @since 2020-06-08 114:48/1.0
 */
@Component
public class LowMemoryMetricsRepository implements MetricsRepository<MetricEntity> {

    private static final long MAX_METRIC_LIVE_TIME_MS = 1000 * 60 * 5;


    /**
     * {@code app -> resource -> timestamp -> metric}
     */
    private Map<String, Map<String, LinkedHashMap<Long, LowMetricEntity>>> allMetrics = new ConcurrentHashMap<>();

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    @Override
    public void save(MetricEntity entity) {
        if (entity == null || StringUtil.isBlank(entity.getApp())) {
            return;
        }
        readWriteLock.writeLock().lock();
        try {
            allMetrics.computeIfAbsent(entity.getApp(), e -> new HashMap<>(16))
                    .computeIfAbsent(entity.getResource(), e -> new LinkedHashMap<Long, LowMetricEntity>() {
                        @Override
                        protected boolean removeEldestEntry(Entry<Long, LowMetricEntity> eldest) {
                            // Metric older than {@link #MAX_METRIC_LIVE_TIME_MS} will be removed.
                            return eldest.getKey() < TimeUtil.currentTimeMillis() - MAX_METRIC_LIVE_TIME_MS;
                        }
                    }).put(entity.getTimestamp().getTime(), LowMetricEntity.toLowMetricEntity(entity));
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        readWriteLock.writeLock().lock();
        try {
            metrics.forEach(this::save);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource,
                                                           long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        Map<String, LinkedHashMap<Long, LowMetricEntity>> resourceMap = allMetrics.get(app);
        if (resourceMap == null) {
            return results;
        }
        LinkedHashMap<Long, LowMetricEntity> metricsMap = resourceMap.get(resource);
        if (metricsMap == null) {
            return results;
        }
        readWriteLock.readLock().lock();
        try {
            for (Entry<Long, LowMetricEntity> entry : metricsMap.entrySet()) {
                if (entry.getKey() >= startTime && entry.getKey() <= endTime) {
                    results.add(LowMetricEntity.toMetricEntity(entry.getValue()));
                }
            }
            return results;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        // resource -> timestamp -> metric
        Map<String, LinkedHashMap<Long, LowMetricEntity>> resourceMap = allMetrics.get(app);
        if (resourceMap == null) {
            return results;
        }
        final long minTimeMs = System.currentTimeMillis() - 1000 * 60;
        Map<String, LowMetricEntity> resourceCount = new ConcurrentHashMap<>(32);

        readWriteLock.readLock().lock();
        try {
            for (Entry<String, LinkedHashMap<Long, LowMetricEntity>> resourceMetrics : resourceMap.entrySet()) {
                for (Entry<Long, LowMetricEntity> metrics : resourceMetrics.getValue().entrySet()) {
                    if (metrics.getKey() < minTimeMs) {
                        continue;
                    }
                    LowMetricEntity newEntity = metrics.getValue();
                    if (resourceCount.containsKey(resourceMetrics.getKey())) {
                        LowMetricEntity oldEntity = resourceCount.get(resourceMetrics.getKey());
                        oldEntity.addPassQps(newEntity.getPassQps());
                        oldEntity.addRtAndSuccessQps(newEntity.getRt(), newEntity.getSuccessQps());
                        oldEntity.addBlockQps(newEntity.getBlockQps());
                        oldEntity.addExceptionQps(newEntity.getExceptionQps());
                        oldEntity.addCount(1);
                    } else {
                        resourceCount.put(resourceMetrics.getKey(), LowMetricEntity.copyOf(newEntity));
                    }
                }
            }
            // Order by last minute b_qps DESC.
            return resourceCount.entrySet()
                    .stream()
                    .sorted((o1, o2) -> {
                        LowMetricEntity e1 = o1.getValue();
                        LowMetricEntity e2 = o2.getValue();
                        int t = e2.getBlockQps() < e1.getBlockQps() ? -1 : ((e2.getBlockQps() == e1.getBlockQps()) ? 0 : 1);
                        if (t != 0) {
                            return t;
                        }
                        return e2.getPassQps() < e1.getPassQps() ? -1 : ((e2.getPassQps() == e1.getPassQps()) ? 0 : 1);
                    })
                    .map(Entry::getKey)
                    .collect(Collectors.toList());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}


class LowMetricEntity {
    private long timestamp;
    private long gmtCreate;

    private int successQps;
    private int exceptionQps;
    private int passQps;
    private int blockQps;

    /**
     * summary rt of all success exit qps.
     */
    private double rt;

    /**
     * 本次聚合的总条数
     */
    private int count;


    public synchronized void addPassQps(int passQps) {
        this.passQps += passQps;
    }

    public synchronized void addBlockQps(int blockQps) {
        this.blockQps += blockQps;
    }

    public synchronized void addExceptionQps(int exceptionQps) {
        this.exceptionQps += exceptionQps;
    }

    public synchronized void addCount(int count) {
        this.count += count;
    }

    public synchronized void addRtAndSuccessQps(double avgRt, int successQps) {
        this.rt += avgRt * successQps;
        this.successQps += successQps;
    }

    public static LowMetricEntity copyOf(LowMetricEntity oldEntity) {
        LowMetricEntity entity = new LowMetricEntity();
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setTimestamp(oldEntity.getTimestamp());
        entity.setPassQps(oldEntity.getPassQps());
        entity.setBlockQps(oldEntity.getBlockQps());
        entity.setSuccessQps(oldEntity.getSuccessQps());
        entity.setExceptionQps(oldEntity.getExceptionQps());
        entity.setRt(oldEntity.getRt());
        entity.setCount(oldEntity.getCount());
        return entity;
    }


    public static LowMetricEntity toLowMetricEntity(MetricEntity metricEntity) {
        LowMetricEntity entity = new LowMetricEntity();
        Optional.ofNullable(metricEntity.getGmtCreate()).ifPresent(v -> entity.setGmtCreate(v.getTime()));
        Optional.ofNullable(metricEntity.getTimestamp()).ifPresent(v -> entity.setTimestamp(v.getTime()));
        entity.setPassQps(metricEntity.getPassQps().intValue());
        entity.setBlockQps(metricEntity.getBlockQps().intValue());
        entity.setSuccessQps(metricEntity.getSuccessQps().intValue());
        entity.setExceptionQps(metricEntity.getExceptionQps().intValue());
        entity.setRt(metricEntity.getRt());
        entity.setCount(metricEntity.getCount());
        return entity;
    }


    public static MetricEntity toMetricEntity(LowMetricEntity lowMetricEntity) {
        MetricEntity entity = new MetricEntity();
        entity.setGmtCreate(new Date(lowMetricEntity.getGmtCreate()));
        entity.setTimestamp(new Date(lowMetricEntity.getTimestamp()));
        entity.setPassQps(Long.valueOf(lowMetricEntity.getPassQps()));
        entity.setBlockQps(Long.valueOf(lowMetricEntity.getBlockQps()));
        entity.setSuccessQps(Long.valueOf(lowMetricEntity.getSuccessQps()));
        entity.setExceptionQps(Long.valueOf(lowMetricEntity.getExceptionQps()));
        entity.setRt(lowMetricEntity.getRt());
        entity.setCount(lowMetricEntity.getCount());
        return entity;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public int getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(int successQps) {
        this.successQps = successQps;
    }

    public int getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(int exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public int getPassQps() {
        return passQps;
    }

    public void setPassQps(int passQps) {
        this.passQps = passQps;
    }

    public int getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(int blockQps) {
        this.blockQps = blockQps;
    }

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
