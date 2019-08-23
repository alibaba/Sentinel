package com.alibaba.csp.sentinel.slots.block.adaptive;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.adaptive.controller.AdaptiveRateController;
import com.alibaba.csp.sentinel.slots.block.adaptive.controller.PidController;
import com.alibaba.csp.sentinel.slots.block.adaptive.controller.TokenBucketController;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Liu Yiming
 * @date 2019-07-27 22:18
 */
public final class AdaptiveRuleUtil {
    /**
     * Build the adaptive rule map from raw list of adaptive rules.
     *
     * @param list raw list of adaptive rules
     * @return constructed new adaptive rule map; empty map if list is null or empty, or no valid rules
     */

    public static Map<String, Set<AdaptiveRule>> buildAdaptiveRuleMap(List<AdaptiveRule> list) {

        Map<String, Set<AdaptiveRule>> newRuleMap = new ConcurrentHashMap<>();

        if (list == null || list.isEmpty()) {
            return newRuleMap;
        }

        for (AdaptiveRule rule : list) {
            if (StringUtil.isBlank(rule.getLimitApp())) {
                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            }

            TrafficShapingController rater = generateRater(rule);
            rule.setRater(rater);

            String identity = rule.getResource();
            Set<AdaptiveRule> ruleSet = newRuleMap.get(identity);
            if (ruleSet == null) {
                ruleSet = new HashSet<>();
                newRuleMap.put(identity, ruleSet);
            }
            ruleSet.add(rule);
        }
        return newRuleMap;
    }

    private static TrafficShapingController generateRater(AdaptiveRule rule) {

        // 如果用户设置使用PID控制器，一律使用PidController
        if(rule.getGrade() == 2) {
            return new PidController(rule.getTargetRatio(), rule.getExpectRt());
        }
        // 根据expectRt判断是适合用漏桶还是用令牌桶
        if (rule.getExpectRt() <= 200) {
            return new TokenBucketController(rule.getTargetRatio(), rule.getExpectRt());
        }
        return new AdaptiveRateController((int)rule.getExpectRt());
    }


}
