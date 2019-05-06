package com.alibaba.csp.sentinel.metric.extension.prometheus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import io.prometheus.client.exporter.HTTPServer;

/**
 * Start the demo and curl localhost:1234 , you could see metrics exposed by
 * Sentinel Prometheus metric extension.
 *
 * @author Carpenter Lee
 */
public class PrometheusMetricExtensionDemo {
    public static void main(String[] args) throws Exception {
        HTTPServer server = new HTTPServer(1234);
        System.out.println("server start...");

        initFlowQpsRule();
        Random random = new Random();
        while (true) {
            Entry entry = null;
            try {
                entry = SphU.entry("resource1");
                Thread.sleep(random.nextInt(1000));
                if (random.nextInt(2) == 1) {
                    throw new RuntimeException("biz exception");
                }
            } catch (BlockException e1) {
                // block
                Thread.sleep(random.nextInt(1000));
            } catch (Exception e2) {
                // biz exception
                Tracer.trace(e2);
            } finally {
                if (entry != null) {
                    entry.exit();
                }
            }
        }
    }

    private static void initFlowQpsRule() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource("resource1");
        rule1.setCount(1);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }
}

