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
import java.util.Arrays;
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

    private volatile String[] types;

    private volatile String identify;

    private volatile int fetchSize;

    private volatile int delayTime;

    @Override
    public List<MetricFamilySamples> collect() {
        int ONE_SECOND = 1000;
        if (searcher == null) {
            synchronized (lock) {
                fetchSize = Integer.parseInt(System.getProperty("sentinel.prometheus.size","1024"));
                delayTime = Integer.parseInt(System.getProperty("sentinel.prometheus.delay","0"));
                identify = System.getProperty("sentinel.prometheus.identify",null);
                String typeStr = System.getProperty("sentinel.prometheus.types","passQps|blockQps|exceptionQps|rt|concurrency");
                types = typeStr.split("\\|");
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

        long endTime = System.currentTimeMillis() / ONE_SECOND * ONE_SECOND - (long) delayTime * ONE_SECOND;
        try {
            List<MetricNode> nodes = searcher.findByTimeAndResource(lastFetchTime, endTime, identify);
            if(nodes == null){
                lastFetchTime = endTime + ONE_SECOND;
                return list;
            }
            if(nodes.size() > fetchSize){
                nodes = nodes.subList(0,fetchSize);
            }
            GaugeMetricFamily exampleGaugeMetricFamily = new GaugeMetricFamily(appName,
                    "sentinel_metrics", Arrays.asList("resource","classification","type"));
            for (MetricNode node : nodes) {
                long recordTime = node.getTimestamp();
                for (String type : types) {
                    double val = getTypeVal(node,type);
                    exampleGaugeMetricFamily.addMetric(Arrays.asList(node.getResource(), String.valueOf(node.getClassification()),type), val,recordTime);
                }
            }
            list.add(exampleGaugeMetricFamily);
            lastFetchTime = endTime + ONE_SECOND;
        } catch (Exception e) {
            RecordLog.warn("[SentinelCollector] failed to fetch sentinel metrics with exception:", e);
        }

        return list;
    }

    public double getTypeVal(MetricNode node,String type){
        if("passQps".equals(type)){
            return node.getPassQps();
        }
        if("blockQps".equals(type)){
            return node.getBlockQps();
        }
        if("successQps".equals(type)){
            return node.getSuccessQps();
        }
        if("exceptionQps".equals(type)){
            return node.getExceptionQps();
        }
        if("rt".equals(type)){
            return node.getRt();
        }
        if("occupiedPassQps".equals(type)){
            return node.getOccupiedPassQps();
        }
        if("concurrency".equals(type)){
            return node.getConcurrency();
        }
        return -1.0;
    }
}
