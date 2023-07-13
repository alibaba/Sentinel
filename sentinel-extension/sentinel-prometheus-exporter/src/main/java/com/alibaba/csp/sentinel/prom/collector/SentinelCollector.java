package com.alibaba.csp.sentinel.prom.collector;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.prom.PromExporterInit;
import com.alibaba.csp.sentinel.util.PidUtil;
import io.prometheus.client.Collector;
import com.alibaba.csp.sentinel.prom.metrics.GaugeMetricFamily;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The{@link PromExporterInit} the Collector for prometheus exporter.
 *
 * @author karl-sy
 * @date 2023-07-13 21:15
 * @since 2.0.0
 */
public class SentinelCollector extends Collector {

    private final Object lock = new Object();
    private volatile MetricSearcher searcher;

    private volatile Long lastFetchTime;

    private volatile String appName;

    @Override
    public List<MetricFamilySamples> collect() {
        int ONE_SECOND = 1000;
        if (searcher == null) {
            synchronized (lock) {
                appName = System.getProperty("sentinel.prometheus.app",SentinelConfig.getAppName());
                if (appName == null) {
                    appName = "SENTINEL_APP";
                }
                if (searcher == null) {
                    searcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,
                            MetricWriter.formMetricFileName(appName, PidUtil.getPid()));
                }
                appName = appName.replaceAll("\\.","_");
                RecordLog.warn("[SentinelCollector] fetch sentinel metrics with appName:{}", appName);
                lastFetchTime = System.currentTimeMillis() / ONE_SECOND * ONE_SECOND;
            }
        }

        List<MetricFamilySamples> list = new ArrayList<>();

        long curTime = System.currentTimeMillis() / ONE_SECOND * ONE_SECOND;
        try {
            List<MetricNode> nodes = searcher.findByTimeAndResource(lastFetchTime, curTime, null);
            if(nodes == null){
                lastFetchTime = curTime + ONE_SECOND;
                return list;
            }
            GaugeMetricFamily exampleGaugeMetricFamily = new GaugeMetricFamily(appName,
                    "sentinel_metrics", Collections.singletonList("type"));
            for (MetricNode node : nodes) {
                long recordTime = node.getTimestamp();
                exampleGaugeMetricFamily.addMetric(Collections.singletonList("pass"), node.getPassQps(),recordTime);
                exampleGaugeMetricFamily.addMetric(Collections.singletonList("rt"), node.getRt(),recordTime);
                exampleGaugeMetricFamily.addMetric(Collections.singletonList("block"), node.getBlockQps(),recordTime);
                exampleGaugeMetricFamily.addMetric(Collections.singletonList("concurrency"), node.getConcurrency(),recordTime);
            }
            list.add(exampleGaugeMetricFamily);
            lastFetchTime = curTime + ONE_SECOND;
        } catch (Exception e) {
            RecordLog.warn("[SentinelCollector] failed to fetch sentinel metrics with exception:", e);
        }

        return list;
    }
}
