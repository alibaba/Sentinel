package com.alibaba.csp.sentinel.metric.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * Get all {@link MetricExtension}s via SPI.
 *
 * @author Carpenter Lee
 * @since 1.6.1
 */
public class MetricExtensionInit implements InitFunc {
    private static List<MetricExtension> metricExtensions = new ArrayList<>();

    @Override
    public void init() throws Exception {
        ServiceLoader<MetricExtension> loader = ServiceLoader.load(MetricExtension.class);
        for (MetricExtension extension : loader) {
            metricExtensions.add(extension);
            RecordLog.info("[MetricExtensionInit] Found MetricExtension: "
                + extension.getClass().getCanonicalName());
        }
    }

    /**
     * Get all metric extensions. DO NOT MODIFY the returned list.
     *
     * @return all metric extensions.
     */
    public static List<MetricExtension> getMetricExtensions() {
        return metricExtensions;
    }
}
