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
        double passQps = node.passQps();
        Queue<Integer> oldCounts = rule.getOldCounts();
        int newLimit = rule.getLimiter().update(oldCounts, minRt, rt, passQps);
        rule.setCount(newLimit);
        updateFlowQpsRule(rule.getResource(), newLimit);
        rule.addCount(newLimit);
        rule.setTimes(0);
    }

    private static void updateFlowQpsRule(String key, int newLimit) {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(key);
        rule1.setCount(newLimit);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }
}
