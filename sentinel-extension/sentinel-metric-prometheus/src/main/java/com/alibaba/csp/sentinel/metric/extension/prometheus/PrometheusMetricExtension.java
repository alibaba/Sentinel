package com.alibaba.csp.sentinel.metric.extension.prometheus;

import com.alibaba.csp.sentinel.metric.extension.MetricExtension;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * Prometheus based Sentinel metric extension. This extension will expose metric
 * in form of Prometheus.
 *
 * @author Carpenter Lee
 * @since 1.6.1
 */
public class PrometheusMetricExtension implements MetricExtension {
    private Counter passRequests;
    private Counter blockRequests;
    private Counter successRequests;
    private Counter exceptionRequests;
    private Histogram rtHist;
    private Gauge currentThreads;

    public PrometheusMetricExtension() {
        passRequests = Counter.build()
            .name("sentinel_pass_requests_total")
            .help("total pass requests.")
            .labelNames("resource")
            .register();
        blockRequests = Counter.build()
            .name("sentinel_block_requests_total")
            .help("total block requests.")
            .labelNames("resource", "type", "ruleLimitApp", "limitApp")
            .register();
        successRequests = Counter.build()
            .name("sentinel_success_requests_total")
            .help("total success requests.")
            .labelNames("resource")
            .register();
        exceptionRequests = Counter.build()
            .name("sentinel_exception_requests_total")
            .help("total exception requests.")
            .labelNames("resource")
            .register();
        currentThreads = Gauge.build()
            .name("sentinel_current_threads")
            .help("current thread count.")
            .labelNames("resource")
            .register();
        rtHist = Histogram.build()
            .name("sentinel_requests_latency_seconds")
            .help("request latency in seconds.")
            .labelNames("resource")
            .register();
    }

    public Counter getPassRequests() {
        return passRequests;
    }

    public Counter getBlockRequests() {
        return blockRequests;
    }

    public Counter getSuccessRequests() {
        return successRequests;
    }

    public Counter getExceptionRequests() {
        return exceptionRequests;
    }

    public Histogram getRtHist() {
        return rtHist;
    }

    public Gauge getCurrentThreads() {
        return currentThreads;
    }

    @Override
    public void addPass(String resource, int n, Object... args) {
        passRequests.labels(resource).inc(n);
    }

    @Override
    public void addBlock(String resource, int n, String origin, BlockException ex, Object... args) {
        blockRequests.labels(resource, ex.getClass().getSimpleName(), ex.getRuleLimitApp(), origin).inc(n);
    }

    @Override
    public void addSuccess(String resource, int n, Object... args) {
        successRequests.labels(resource).inc(n);
    }

    @Override
    public void addException(String resource, int n, Throwable throwable) {
        exceptionRequests.labels(resource).inc(n);
    }

    @Override
    public void addRt(String resource, long rt, Object... args) {
        // convert millisecond to second
        rtHist.labels(resource).observe(((double)rt) / 1000);
    }

    @Override
    public void increaseThreadNum(String resource, Object... args) {
        currentThreads.labels(resource).inc();
    }

    @Override
    public void decreaseThreadNum(String resource, Object... args) {
        currentThreads.labels(resource).dec();
    }
}
