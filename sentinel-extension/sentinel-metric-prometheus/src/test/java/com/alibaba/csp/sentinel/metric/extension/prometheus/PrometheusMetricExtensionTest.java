package com.alibaba.csp.sentinel.metric.extension.prometheus;

import java.util.List;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.junit.Assert;
import org.junit.Test;

public class PrometheusMetricExtensionTest {

    private String r = "resource1";

    @Test
    public void addPass() {
        PrometheusMetricExtension p = new PrometheusMetricExtension();
        int n = 4;
        p.addPass(r, n);
        Counter requests = p.getPassRequests();
        List<MetricFamilySamples> collect = requests.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(n, value, 0.001);
    }

    @Test
    public void addBlock() {
        PrometheusMetricExtension p = new PrometheusMetricExtension();
        int n = 5;
        p.addBlock(r, n, "origin1", new FlowException("default"));
        Counter requests = p.getBlockRequests();
        List<MetricFamilySamples> collect = requests.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(n, value, 0.001);
    }

    @Test
    public void addSuccess() {
        PrometheusMetricExtension p = new PrometheusMetricExtension();
        int n = 6;
        p.addSuccess(r, n);
        Counter requests = p.getSuccessRequests();
        List<MetricFamilySamples> collect = requests.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(n, value, 0.001);
    }

    @Test
    public void addException() {
        PrometheusMetricExtension p = new PrometheusMetricExtension();
        int n = 7;
        p.addException(r, n, new RuntimeException("bizException"));
        Counter requests = p.getExceptionRequests();
        List<MetricFamilySamples> collect = requests.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(n, value, 0.001);
    }

    @Test
    public void increaseThreadNum() {
        PrometheusMetricExtension p = new PrometheusMetricExtension();
        p.increaseThreadNum(r);
        p.increaseThreadNum(r);
        p.increaseThreadNum(r);
        p.increaseThreadNum(r);
        Gauge threads = p.getCurrentThreads();
        List<MetricFamilySamples> collect = threads.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(4, value, 0.001);
    }

    @Test
    public void decreaseThreadNum() {
        PrometheusMetricExtension p = new PrometheusMetricExtension();
        p.increaseThreadNum(r);
        p.increaseThreadNum(r);
        p.increaseThreadNum(r);
        p.increaseThreadNum(r);

        p.decreaseThreadNum(r);
        Gauge threads = p.getCurrentThreads();
        List<MetricFamilySamples> collect = threads.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(3, value, 0.001);
    }
}