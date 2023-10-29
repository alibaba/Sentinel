package com.alibaba.csp.sentinel.slots.adaptive;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * @author ElonTusk
 * @name AdaptiveLimiter
 * @date 2023/8/7 14:14
 */
public class AdaptiveLimiter {
    public static void adaptiveLimit(AdaptiveRule rule) {
        ClusterNode node = ClusterBuilderSlot.getClusterNode(rule.getResource(), EntryType.IN);
        if (node != null) {
            adaptiveLimit(rule, node);
        }
    }

    public static void adaptiveLimit(AdaptiveRule rule, StatisticNode node) {
        double minRt = node.minRt();
        double rt = node.avgRt();
        if (rt == 0 || minRt == 1) {
            return;
        }
        int newLimit = rule.getLimiter().update(rule, node);
        rule.setCount(newLimit);
        updateFlowQpsRule(rule.getFlowId(), rule.getResource(), newLimit);
        rule.addCount(newLimit);
        rule.setTimes(0);
    }

    private static void updateFlowQpsRule(long flowId, String key, int newLimit) {
        FlowRule updateFlow = new FlowRule();
        updateFlow.setId(flowId);
        updateFlow.setResource(key);
        updateFlow.setCount(newLimit);
        updateFlow.setGrade(RuleConstant.FLOW_GRADE_QPS);
        updateFlow.setLimitApp("default");
        List<FlowRule> updateFlows = new ArrayList<>();
        updateFlows.add(updateFlow);
        FlowRuleManager.loadRules(updateFlows);
    }
}
