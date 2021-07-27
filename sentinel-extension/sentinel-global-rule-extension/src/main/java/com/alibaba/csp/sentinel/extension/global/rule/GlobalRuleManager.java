package com.alibaba.csp.sentinel.extension.global.rule;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
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

    private static volatile Map<String, List<CircuitBreaker>> degradeRuleMap = new HashMap<>();

    private static volatile Map<String, List<AuthorityRule>> authorityRuleMap = new HashMap<>();

    private static ReentrantLock flowRuleUpdateLock = new ReentrantLock();

    private static ReentrantLock degradeRuleUpdateLock = new ReentrantLock();

    private static ReentrantLock authorityRuleUpdateLock = new ReentrantLock();

    public static void updateGlobalFlowRules(Map<String, List<FlowRule>> flowRuleMap) {
        flowRuleUpdateLock.lock();
        try {
            GlobalRuleManager.flowRuleMap = flowRuleMap;
        } finally {
            flowRuleUpdateLock.unlock();
        }
    }

    public static Map<String, List<FlowRule>> getGlobalFlowRules() {
        return flowRuleMap;
    }

    public static void updateGlobalDegradeRules(Map<String, List<CircuitBreaker>> degradeRuleMap) {
        degradeRuleUpdateLock.lock();
        try {
            GlobalRuleManager.degradeRuleMap = degradeRuleMap;
        } finally {
            degradeRuleUpdateLock.unlock();
        }
    }

    public static Map<String, List<CircuitBreaker>> getGlobalDegradeRules() {
        return degradeRuleMap;
    }

    public static void updateGlobalAuthorityRules(Map<String, List<AuthorityRule>> authorityRuleMap) {
        authorityRuleUpdateLock.lock();
        try {
            GlobalRuleManager.authorityRuleMap = authorityRuleMap;
        } finally {
            authorityRuleUpdateLock.unlock();
        }
    }

    public static Map<String, List<AuthorityRule>> getGlobalAuthorityRules() {
        return authorityRuleMap;
    }
}
