package com.alibaba.csp.sentinel.transport;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.CommandCenterLog;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;

public class MetricWriterPrometheusImpl implements MetricWriter {

    private final PushGateway pushGateway = new PushGateway(ConfigUtils.getPrometheusAddress());

    @Override
    public void write(long time, List<MetricNode> nodes) throws Exception {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        MetricNode node = nodes.get(nodes.size() - 1);
        List<PrometheusKpi> kpiList = convertToPrometheusKpi(node);
        if (kpiList == null || kpiList.isEmpty()) {
            return;
        }

        CollectorRegistry registry = new CollectorRegistry();
        for (PrometheusKpi kpi : kpiList) {
            Map<String, String> attach = kpi.getAttach();
            if (attach == null || attach.isEmpty()) {
                continue;
            }

            List<Entry<String, String>> entries = new LinkedList<Entry<String, String>>();
            entries.addAll(kpi.getAttach().entrySet());
            String[] labelNames = new String[entries.size()];
            String[] labelValues = new String[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                labelNames[i] = entries.get(i).getKey();
                labelValues[i] = entries.get(i).getValue();
            }

            Gauge gauge = Gauge.build()
                    .name(kpi.getName())
                    .labelNames(labelNames)
                    .help(kpi.getDescribe())
                    .create().register(registry);

            gauge.labels(labelValues).set(kpi.getValue());

            try {
                pushGateway.push(registry, "sentinel");
            } catch (IOException e) {
                CommandCenterLog.warn("推送异常: " + gauge, e);
            }
        }
    }

    private List<PrometheusKpi> convertToPrometheusKpi(MetricNode node) {
        String host = "";
        String port = String.valueOf(ConfigUtils.getAppWebPort());
        String instance = host + ":" + port;

        Map<String, String> attach = new HashMap<String, String>();
        attach.put("language", "java");
        attach.put("appName", SentinelConfig.getAppName());
        attach.put("host", host);
        attach.put("port", port);
        attach.put("instance", instance);
        attach.put("resource", node.getResource());

        List<PrometheusKpi> list = new LinkedList<PrometheusKpi>();
        PrometheusKpi blockedQpsKpi = PrometheusKpi.builder()
                .describe("app blocked qps.")
                .name("SENTINEL_BLOCKED_QPS")
                .attach(attach)
                .value(node.getBlockedQps())
                .build();
        PrometheusKpi exceptionKpi = PrometheusKpi.builder()
                .describe("app exception count.")
                .name("SENTINEL_EXCEPTION_COUNT")
                .attach(attach)
                .value(node.getException())
                .build();
        PrometheusKpi passedQpsKpi = PrometheusKpi.builder()
                .describe("app passed qps.")
                .name("SENTINEL_PASSED_QPS")
                .attach(attach)
                .value(node.getPassedQps())
                .build();
        PrometheusKpi rtKpi = PrometheusKpi.builder()
                .describe("app response time.")
                .name("SENTINEL_RT")
                .attach(attach)
                .value(node.getRt())
                .build();
        PrometheusKpi successQpsKpi = PrometheusKpi.builder()
                .describe("app success qps.")
                .name("SENTINEL_SUCCESS_QPS")
                .attach(attach)
                .value(node.getSuccessQps())
                .build();
        list.add(blockedQpsKpi);
        list.add(exceptionKpi);
        list.add(passedQpsKpi);
        list.add(rtKpi);
        list.add(successQpsKpi);
        return list;
    }

    private static class PrometheusKpi {

        private String name;

        private Map<String, String> attach;

        private Long value;

        private String describe;

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        Map<String, String> getAttach() {
            return attach;
        }

        void setAttach(Map<String, String> attach) {
            this.attach = attach;
        }

        Long getValue() {
            return value;
        }

        void setValue(Long value) {
            this.value = value;
        }

        String getDescribe() {
            return describe;
        }

        void setDescribe(String describe) {
            this.describe = describe;
        }

        @Override
        public String toString() {
            return "PrometheusKpi{" +
                    "name='" + name + '\'' +
                    ", attach=" + attach +
                    ", value=" + value +
                    ", describe='" + describe + '\'' +
                    '}';
        }

        static PrometheusKpiBuilder builder() {
            return new PrometheusKpiBuilder();
        }

    }

    static class PrometheusKpiBuilder extends PrometheusKpi {

        PrometheusKpiBuilder describe(String describe) {
            super.describe = describe;
            return this;
        }

        PrometheusKpiBuilder name(String name) {
            super.name = name;
            return this;
        }

        PrometheusKpiBuilder attach(Map<String, String> attach) {
            super.attach = attach;
            return this;
        }

        PrometheusKpiBuilder value(long value) {
            super.value = value;
            return this;
        }

        PrometheusKpi build() {
            PrometheusKpi kpi = new PrometheusKpi();
            kpi.setAttach(this.getAttach());
            kpi.setDescribe(this.getDescribe());
            kpi.setName(this.getName());
            kpi.setValue(this.getValue());
            return kpi;
        }
    }
}
