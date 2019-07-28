package com.alibaba.csp.sentinel.slots.block.adaptive;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemStatusListener;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Liu Yiming
 * @date 2019-07-16 16:28
 */
public class AdaptiveRuleChecker {

    public void checkAdaptive(ResourceWrapper resource, Context context, DefaultNode node, int count)
        throws BlockException {

        Set<AdaptiveRule> rules = AdaptiveRuleManager.getRules(resource);
        if (rules == null || resource == null) {
            return;
        }

        for (AdaptiveRule rule : rules) {
            if (!canPassCheck(rule, context, node, count)) {
                throw new AdaptiveException(rule.getLimitApp(), rule);
            }
        }
    }

    public boolean canPassCheck(AdaptiveRule rule, Context context, DefaultNode node, int acquireCount) {

        Node selectedNode = node.getClusterNode();
        if (selectedNode == null) { return true; }

        return rule.getRater().canPass(selectedNode, acquireCount, false);
    }

}
