package com.alibaba.csp.sentinel.extension.global.rule.flow;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.extension.global.rule.GlobalRuleManager;
import com.alibaba.csp.sentinel.extension.global.rule.config.GlobalRuleConfig;
import com.alibaba.csp.sentinel.slots.block.flow.DefaultFlowRuleSelector;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : jiez
 * @date : 2021/7/22 15:05
 */
public class GlobalFlowRuleSelector extends DefaultFlowRuleSelector {

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
    public List<FlowRule> select(String resource) {
        List<FlowRule> matchedRules = new ArrayList<>();
        List<FlowRule> matchedNormalRules = super.select(resource);
        if (Objects.nonNull(matchedNormalRules)) {
            matchedRules.addAll(matchedNormalRules);
        }
        if (isCanMergingRule() || (Objects.isNull(matchedNormalRules)) || matchedNormalRules.size() <= 0) {
            Map<String, List<FlowRule>> globalFlowMap = GlobalRuleManager.getGlobalFlowRules();
            for (Map.Entry<String, List<FlowRule>> globalFlowEntry : globalFlowMap.entrySet()) {
                List<FlowRule> globalFlowRules = globalFlowEntry.getValue();
                if (matchGlobalRuleByRegularExpression(globalFlowEntry.getKey(), resource)
                        && Objects.nonNull(globalFlowRules)) {
                    matchedRules.addAll(globalFlowRules);
                }
            }
        }
        return matchedRules;
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
        return Boolean.parseBoolean(SentinelConfig.getConfig(GlobalRuleConfig.GLOBAL_RULE_MERGING_FLOW_RULE));
    }
}
