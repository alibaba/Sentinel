package com.alibaba.csp.sentinel.metric.prom.collector;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import org.junit.Assert;
import org.junit.Test;

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
