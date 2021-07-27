package com.alibaba.csp.sentinel.extension.global.rule.degrade;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.extension.global.rule.GlobalRuleManager;
import com.alibaba.csp.sentinel.extension.global.rule.config.GlobalRuleConfig;
import com.alibaba.csp.sentinel.slots.block.degrade.DefaultDegradeRuleSelector;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author : jiez
 * @date : 2021/7/22 15:05
 */
public class GlobalDegradeRuleSelector extends DefaultDegradeRuleSelector {

    private Map<String, Pattern> patternCacheMap = new ConcurrentHashMap<>(16);

    /**
     * return abstract class impl priority
     *
     * @return int priority
     */
    @Override
    public int getPriority() {
        return 10;
    }

    /**
     * select matched flow rule
     *
     * @param resource resource name
     * @return list of match rule
     */
    @Override
    public List<CircuitBreaker> select(String resource) {
        List<CircuitBreaker> matchedCircuitBreakers = new ArrayList<>();
        List<CircuitBreaker> matchedNormalBreakers = super.select(resource);
        if (Objects.nonNull(matchedNormalBreakers)) {
            matchedCircuitBreakers.addAll(matchedNormalBreakers);
        }

        if (isCanMergingRule() || (Objects.isNull(matchedNormalBreakers)) || matchedNormalBreakers.size() <= 0) {
            Map<String, List<CircuitBreaker>> globalCircuitBreakerMap = GlobalRuleManager.getGlobalDegradeRules();
            for (Map.Entry<String, List<CircuitBreaker>> globalCircuitBreakerEntry : globalCircuitBreakerMap.entrySet()) {
                List<CircuitBreaker> globalCircuitBreakers = globalCircuitBreakerEntry.getValue();
                if (matchGlobalRuleByRegularExpression(globalCircuitBreakerEntry.getKey(), resource)
                        && Objects.nonNull(globalCircuitBreakers)) {
                    matchedCircuitBreakers.addAll(globalCircuitBreakers);
                }
            }
        }
        return matchedCircuitBreakers;
    }

    /**
     * regular Expression Matching
     *
     * @param regularExpression regular Expression
     * @param resourceName resource name
     * @return boolean is match
     */
    private boolean matchGlobalRuleByRegularExpression(String regularExpression, String resourceName) {
        Pattern pattern = patternCacheMap.get(regularExpression);
        if (Objects.isNull(pattern)) {
            pattern = Pattern.compile(regularExpression);
            patternCacheMap.put(regularExpression, pattern);
        }
        return pattern.matcher(resourceName).matches();
    }

    /**
     * is need merge normal rule and global rule
     *
     * @return boolean is need merge
     */
    private boolean isCanMergingRule() {
        return Boolean.parseBoolean(SentinelConfig.getConfig(GlobalRuleConfig.GLOBAL_RULE_MERGING_DEGRADE_RULE));
    }
}
