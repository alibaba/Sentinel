package com.alibaba.csp.sentinel.extension.rule;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : jiez
 * @date : 2021/7/21 17:23
 */
public class GlobalRuleManager {

    private static volatile Map<String, List<FlowRule>> flowRuleMap = new HashMap<>();

    private static ReentrantLock flowRuleUpdateLoad = new ReentrantLock();

    public static void updateGlobalFlowRules(Map<String, List<FlowRule>> flowRuleMap) {
        try {
            flowRuleUpdateLoad.lock();
            GlobalRuleManager.flowRuleMap = flowRuleMap;
        } finally {
            flowRuleUpdateLoad.unlock();
        }
    }

    public static Map<String, List<FlowRule>> getGlobalFlowRules() {
        return flowRuleMap;
    }
}
