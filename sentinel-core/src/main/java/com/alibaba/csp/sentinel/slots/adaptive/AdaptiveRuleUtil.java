package com.alibaba.csp.sentinel.slots.adaptive;

import com.alibaba.csp.sentinel.slots.adaptive.algorithm.AbstractLimit;
import com.alibaba.csp.sentinel.slots.adaptive.algorithm.BRPCLimit;
import com.alibaba.csp.sentinel.slots.adaptive.algorithm.GradientLimit;
import com.alibaba.csp.sentinel.slots.adaptive.algorithm.VegasLimit;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ElonTusk
 * @name AdaptiveRuleUtil
 * @date 2023/8/5 16:09
 */
public class AdaptiveRuleUtil {
    public static Map<String, AdaptiveRule> buildAdaptiveRuleMap(List<AdaptiveRule> list) {
        Map<String, AdaptiveRule> newRuleMap = new ConcurrentHashMap<>();
        if (list == null || list.isEmpty()) {
            return newRuleMap;
        }
        for (AdaptiveRule rule : list) {
            AbstractLimit limiter = generateRater(rule);
            rule.setLimiter(limiter);

            String key = rule.getResource();
            newRuleMap.put(key, rule);
        }
        return newRuleMap;
    }

    private static AbstractLimit generateRater(AdaptiveRule rule) {
        switch (rule.getStrategy()) {
            case RuleConstant.ADAPTIVE_VEGAS:
                return VegasLimit.getInstance();
            case RuleConstant.ADAPTIVE_GRADIENT:
                return GradientLimit.getInstance();
            case RuleConstant.ADAPTIVE_BRPC:
                return BRPCLimit.getInstance();
        }
        return VegasLimit.getInstance();
    }
}
