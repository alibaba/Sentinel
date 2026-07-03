package com.alibaba.csp.sentinel.metric.prom.types;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class GaugeMetricFamilyTest {

    @Test
    public void testGaugeMetricFamily(){

        GaugeMetricFamily metricFamily = new GaugeMetricFamily("appName",
                "sentinel_metrics", Collections.singletonList("type"));
        metricFamily.addMetric(Collections.singletonList("rt"), 1.0,System.currentTimeMillis());
        Assert.assertEquals(metricFamily.samples.get(0).value, 1.0,1e-4);
    }
}
