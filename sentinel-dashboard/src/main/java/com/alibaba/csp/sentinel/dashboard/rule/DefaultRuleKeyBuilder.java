package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author cdfive
 */
@Component("defaultRuleKeyBuilder")
public class DefaultRuleKeyBuilder<T> implements RuleKeyBuilder<T> {

    private final static Map<Class, String> ruleNameMap = ImmutableMap.of(
            FlowRuleEntity.class, "flow",
            DegradeRuleEntity.class, "degrade",
            SystemRuleEntity.class, "system"
    );

    private final static String RULES = "rules";

    @Override
    public String buildRuleKey(String app, String ip, Integer port) {
        ResolvableType resolvableType = ResolvableType.forClass(this.getClass());
        Class<?> clazz = resolvableType.getGeneric(0).resolve();
        String ruleName = ruleNameMap.get(clazz);
        return Joiner.on("-").join(app, ip, port, ruleName, RULES);
    }
}
