package com.alibaba.csp.sentinel.extension.rule.flow;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.extension.rule.GlobalRuleManager;
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

    @Override
    public int getPriority() {
        return 10;
    }

    /**
     * todo 添加配置判断是替换还是兼容
     *
     * @param resource
     * @return
     */
    @Override
    public List<FlowRule> select(String resource) {
        List<FlowRule> matchedRules = new ArrayList<>();
        List<FlowRule> matchedNormalRules = super.select(resource);
        if (Objects.nonNull(matchedNormalRules)) {
            matchedRules.addAll(matchedNormalRules);
        }
        if (true || (Objects.isNull(matchedNormalRules)) || matchedNormalRules.size() <= 0) {
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

    private boolean matchGlobalRuleByRegularExpression(String matchRule, String resourceName) {
        Pattern pattern = patternCacheMap.get(matchRule);
        if (Objects.isNull(pattern)) {
            pattern = Pattern.compile(matchRule);
            patternCacheMap.put(matchRule, pattern);
        }
        return pattern.matcher(resourceName).matches();
    }
}
