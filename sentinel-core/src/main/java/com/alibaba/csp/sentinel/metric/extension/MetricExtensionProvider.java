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
package com.alibaba.csp.sentinel.metric.extension;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.SpiLoader;

/**
 * Get all {@link MetricExtension} via SPI.
 *
 * @author Carpenter Lee
 * @since 1.6.1
 */
public class MetricExtensionProvider {
    private static List<MetricExtension> metricExtensions = new ArrayList<>();

    static {
        resolveInstance();
    }

    private static void resolveInstance() {
        List<MetricExtension> extensions = SpiLoader.loadInstanceList(MetricExtension.class);

        if (extensions.isEmpty()) {
            RecordLog.info("[MetricExtensionProvider] No existing MetricExtension found");
        } else {
            metricExtensions.addAll(extensions);
            RecordLog.info("[MetricExtensionProvider] MetricExtension resolved, size=" + extensions.size());
        }
    }

    /**
     * <p>Get all registered metric extensions.</p>
     * <p>DO NOT MODIFY the returned list, use {@link #addMetricExtension(MetricExtension)}.</p>
     *
     * @return all registered metric extensions
     */
    public static List<MetricExtension> getMetricExtensions() {
        return metricExtensions;
    }

    /**
     * Add metric extension.
     * <p>
     * Note that this method is NOT thread safe.
     * </p>
     *
     * @param metricExtension the metric extension to add.
     */
    public static void addMetricExtension(MetricExtension metricExtension) {
        metricExtensions.add(metricExtension);
    }

}
