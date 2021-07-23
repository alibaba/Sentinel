package com.alibaba.csp.sentinel.extension.rule.authority;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.extension.rule.GlobalRuleManager;
import com.alibaba.csp.sentinel.extension.rule.config.GlobalRuleConfig;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.DefaultAuthorityRuleSelector;
import com.alibaba.csp.sentinel.slots.block.degrade.DefaultDegradeRuleSelector;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

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
public class GlobalAuthorityRuleSelector extends DefaultAuthorityRuleSelector {

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
    public List<AuthorityRule> select(String resource) {
        List<AuthorityRule> matchedRules = new ArrayList<>();
        List<AuthorityRule> matchedNormalRules = super.select(resource);
        if (Objects.nonNull(matchedNormalRules)) {
            matchedRules.addAll(matchedNormalRules);
        }
        if (isCanMergingRule() || (Objects.isNull(matchedNormalRules)) || matchedNormalRules.size() <= 0) {
            Map<String, List<AuthorityRule>> globalAuthorityRuleMap = GlobalRuleManager.getGlobalAuthorityRules();
            for (Map.Entry<String, List<AuthorityRule>> globalAuthorityRuleEntry : globalAuthorityRuleMap.entrySet()) {
                List<AuthorityRule> globalAuthorityRules = globalAuthorityRuleEntry.getValue();
                if (matchGlobalRuleByRegularExpression(globalAuthorityRuleEntry.getKey(), resource)
                        && Objects.nonNull(globalAuthorityRules)) {
                    matchedRules.addAll(globalAuthorityRules);
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
        return Boolean.parseBoolean(SentinelConfig.getConfig(GlobalRuleConfig.GLOBAL_RULE_MERGING_AUTHORITY_RULE));
    }

}
