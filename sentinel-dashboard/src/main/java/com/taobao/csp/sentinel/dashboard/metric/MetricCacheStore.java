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
package com.taobao.csp.sentinel.dashboard.metric;

import com.taobao.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.taobao.csp.sentinel.dashboard.datasource.repository.MetricRepository;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Store metrics in memory.
 *
 * @author huyong
 */
@Component
public class MetricCacheStore {

    @Autowired
    private MetricRepository repository;

    private static final long METRIC_DURATION = 1000 * 60;

    public void saveAll(Iterable<MetricEntity> metrics) {
        repository.save(metrics);
    }

    public synchronized List<MetricEntity> queryByAppAndResource(String app,
            String resource,
            long startTime,
            long endTime) {
        return repository
                .findByResourceAndAppAndTime(
                        Date.from(Instant.ofEpochMilli(startTime)),
                        Date.from(Instant.ofEpochMilli(endTime)), app, resource);
    }

    public synchronized List<String> findResourcesOfApp(String app) {
        Map<String, MetricEntity> resourceCount = new HashMap<>(32);

        long startTime = System.currentTimeMillis() - METRIC_DURATION;
        List<MetricEntity> entities = repository
                .findByResourceAndTime(Date.from(Instant.ofEpochMilli(startTime)),
                        new Date(), app);

        for (MetricEntity newEntity : entities) {
            if (resourceCount.containsKey(newEntity.getResource())) {
                MetricEntity oldEntity = resourceCount.get(newEntity.getResource());
                oldEntity.addPassedQps(newEntity.getPassedQps());
                oldEntity.addRtAndSuccessQps(newEntity.getRt(), newEntity.getSuccessQps());
                oldEntity.addBlockedQps(newEntity.getBlockedQps());
                oldEntity.addException(newEntity.getException());
                oldEntity.addCount(1);
            } else {
                resourceCount.put(newEntity.getResource(), MetricEntity.copyOf(newEntity));
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
