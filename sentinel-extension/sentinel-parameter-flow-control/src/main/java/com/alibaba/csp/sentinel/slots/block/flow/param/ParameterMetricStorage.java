/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 * @since 1.6.1
 */
public final class ParameterMetricStorage {

    private static final Map<String, ParameterMetric> metricsMap = new ConcurrentHashMap<>();

    /**
     * Lock for a specific resource.
     */
    private static final Object LOCK = new Object();

    /**
     * Init the parameter metric and index map for given resource.
     * Package-private for test.
     *
     * @param resourceWrapper resource to init
     * @param rule            relevant rule
     */
    public static void initParamMetricsFor(ResourceWrapper resourceWrapper, /*@Valid*/ ParamFlowRule rule) {
        if (resourceWrapper == null || resourceWrapper.getName() == null) {
            return;
        }
        String resourceName = resourceWrapper.getName();
        ParameterMetric metric;
        // Assume that the resource is valid.
        if ((metric = metricsMap.get(resourceName)) == null) {
            synchronized (LOCK) {
                if ((metric = metricsMap.get(resourceName)) == null) {
                    metric = new ParameterMetric();
                    metricsMap.put(resourceWrapper.getName(), metric);
                    RecordLog.info("[ParameterMetricStorage] Creating parameter metric for: " + resourceWrapper.getName());
                }
            }
        }
        metric.initialize(rule);
    }

    public static ParameterMetric getParamMetric(ResourceWrapper resourceWrapper) {
        if (resourceWrapper == null || resourceWrapper.getName() == null) {
            return null;
        }
        return metricsMap.get(resourceWrapper.getName());
    }

    public static ParameterMetric getParamMetricForResource(String resourceName) {
        if (resourceName == null) {
            return null;
        }
        return metricsMap.get(resourceName);
    }

    public static void clearParamMetricForResource(String resourceName) {
        if (StringUtil.isBlank(resourceName)) {
            return;
        }
        metricsMap.remove(resourceName);
        RecordLog.info("[ParameterMetricStorage] Clearing parameter metric for: " + resourceName);
    }

    static Map<String, ParameterMetric> getMetricsMap() {
        return metricsMap;
    }

    private ParameterMetricStorage() {}
}
