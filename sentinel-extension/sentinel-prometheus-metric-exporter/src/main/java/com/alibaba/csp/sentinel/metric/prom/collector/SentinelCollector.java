package com.alibaba.csp.sentinel.metric.prom.collector;

/**
 * The{@link PromExporterInit} the Collector for prometheus exporter.
 *
 * @author karl-sy
 * @date 2023-07-13 21:15
 * @since 2.0.0
 */
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.metric.prom.config.PrometheusGlobalConfig;
import com.alibaba.csp.sentinel.metric.prom.types.GaugeMetricFamily;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.util.PidUtil;
import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SentinelCollector extends Collector {

    private final Object lock = new Object();

    private static final int ONE_SECOND = 1000;
    private static final String appName = PrometheusGlobalConfig.getPromFetchApp();

    private static final String[] types =  PrometheusGlobalConfig.getPromFetchTypes();

    private static final String identify =  PrometheusGlobalConfig.getPromFetchIdentify();

    private static final int fetchSize = PrometheusGlobalConfig.getPromFetchSize();

    private static final int delayTime = PrometheusGlobalConfig.getPromFetchDelayTime();

    private volatile MetricSearcher searcher;

    private volatile Long lastFetchTime;

    @Override
    public List<MetricFamilySamples> collect() {
        if (searcher == null) {
            synchronized (lock) {
                if (searcher == null) {
                    searcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,
                            MetricWriter.formMetricFileName(SentinelConfig.getAppName(), PidUtil.getPid()));
                }
                RecordLog.warn("[SentinelCollector] init sentinel metrics searcher with appName:{}", appName);
                lastFetchTime = System.currentTimeMillis() / ONE_SECOND * ONE_SECOND;
            }
        }

        List<MetricFamilySamples> list = new ArrayList<>();

        long endTime = System.currentTimeMillis() / ONE_SECOND * ONE_SECOND - (long) delayTime * ONE_SECOND;
        try {
            List<MetricNode> nodes = searcher.findByTimeAndResource(lastFetchTime, endTime, identify);
            if(nodes == null){
                return list;
            }
            if(nodes.size() > fetchSize){
                nodes = nodes.subList(0,fetchSize);
            }
            GaugeMetricFamily metricFamily = new GaugeMetricFamily(appName,
                    "sentinel_metrics", Arrays.asList("resource","classification","type"));
            for (MetricNode node : nodes) {
                long recordTime = node.getTimestamp();
                for (String type : types) {
                    double val = getTypeVal(node,type);
                    metricFamily.addMetric(Arrays.asList(node.getResource(), String.valueOf(node.getClassification()),type), val,recordTime);
                }
            }
            list.add(metricFamily);
        } catch (Exception e) {
            RecordLog.warn("[SentinelCollector] failed to fetch sentinel metrics with exception:", e);
        }finally {
            lastFetchTime = endTime + ONE_SECOND;
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
