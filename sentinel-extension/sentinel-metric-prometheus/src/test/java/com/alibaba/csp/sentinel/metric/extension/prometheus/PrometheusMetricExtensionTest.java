package com.alibaba.csp.sentinel.metric.extension.prometheus;

import java.util.List;

import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.metric.extension.MetricExtensionProvider;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrometheusMetricExtensionTest {

    private String r = "resource1";
    private PrometheusMetricExtension p;

    @Before
    public void before() {
        for (MetricExtension extension : MetricExtensionProvider.getMetricExtensions()) {
            if (extension instanceof PrometheusMetricExtension) {
                p = (PrometheusMetricExtension)extension;
                break;
            }
        }
        if (p == null) {
            p = new PrometheusMetricExtension();
        }
    }

    @Test
    public void addPass() {
        p.getPassRequests().clear();
        int n = 4;
        p.addPass(r, n);
        Counter requests = p.getPassRequests();
        List<MetricFamilySamples> collect = requests.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(n, value, 0.001);
    }

    @Test
    public void addBlock() {
        p.getBlockRequests().clear();
        int n = 5;
        p.addBlock(r, n, "origin1", new FlowException("default"));
        Counter requests = p.getBlockRequests();
        List<MetricFamilySamples> collect = requests.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(n, value, 0.001);
    }

    @Test
    public void addSuccess() {
        p.getSuccessRequests().clear();
        int n = 6;
        p.addSuccess(r, n);
        Counter requests = p.getSuccessRequests();
        List<MetricFamilySamples> collect = requests.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(n, value, 0.001);
    }

    @Test
    public void addException() {
        p.getExceptionRequests().clear();
        int n = 7;
        p.addException(r, n, new RuntimeException("bizException"));
        Counter requests = p.getExceptionRequests();
        List<MetricFamilySamples> collect = requests.collect();
        double value = collect.get(0).samples.get(0).value;
        Assert.assertEquals(n, value, 0.001);
    }

    @Test
    public void increaseThreadNum() {
        p.getCurrentThreads().clear();
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
        p.getCurrentThreads().clear();
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