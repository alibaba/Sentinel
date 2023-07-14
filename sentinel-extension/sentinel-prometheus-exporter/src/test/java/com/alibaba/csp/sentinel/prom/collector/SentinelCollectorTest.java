package com.alibaba.csp.sentinel.prom.collector;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.prom.metrics.GaugeMetricFamily;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class SentinelCollectorTest {
    @Test
    public void testCollector(){
        SentinelCollector collector = new SentinelCollector();

        MetricNode node = new MetricNode();
        node.setPassQps(10);
        double val = collector.getTypeVal(node,"passQps");
        Assert.assertEquals(val, 10,1e-4);
    }
}
