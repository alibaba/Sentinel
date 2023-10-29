package com.alibaba.csp.sentinel.slots.adaptive;

import java.util.Map;

/**
 * @author ElonTusk
 * @name AdaptiveListener
 * @date 2023/8/3 13:24
 */
public class AdaptiveTask implements Runnable {

    @Override
    public void run() {
        Map<String, AdaptiveRule> adaptiveRules = AdaptiveRuleManager.getAdaptiveRules();
        for (AdaptiveRule rule : adaptiveRules.values()) {
            AdaptiveLimiter.adaptiveLimit(rule);
        }
    }
}
