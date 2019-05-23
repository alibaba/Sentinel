package com.alibaba.csp.sentinel.metric.extension;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.SpiLoader;

/**
 * Get all {@link MetricExtension}s via SPI.
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

        if (extensions == null) {
            RecordLog.warn("[MetricExtensionProvider] WARN: No existing MetricExtension found");
        } else {
            metricExtensions.addAll(extensions);
            RecordLog.info("[MetricExtensionProvider] MetricExtension resolved, size=" + extensions.size());
        }
    }

    /**
     * Get all metric extensions. DO NOT MODIFY the returned list, use {@link #addMetricExtension(MetricExtension)}.
     *
     * @return all metric extensions.
     */
    public static List<MetricExtension> getMetricExtensions() {
        return metricExtensions;
    }

    /**
     * Add metric extension.
     * <p>
     * Not that this method is NOT thread safe.
     * </p>
     *
     * @param metricExtension the metric extension to add.
     */
    public static void addMetricExtension(MetricExtension metricExtension) {
        metricExtensions.add(metricExtension);
    }

}
