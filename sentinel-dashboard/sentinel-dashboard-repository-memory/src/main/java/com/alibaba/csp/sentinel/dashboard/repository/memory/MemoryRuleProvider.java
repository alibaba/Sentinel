package com.alibaba.csp.sentinel.dashboard.repository.memory;

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRuleProvider;
import com.alibaba.csp.sentinel.transport.client.SentinelApiClient;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import java.util.Map;

/**
 * @author cdfive
 */
public class MemoryRuleProvider<T extends RuleEntity> extends AbstractRuleProvider<T> {

    @Autowired
    private SentinelApiClient sentinelApiClient;

    private final static Map<Class, String> RULE_NAME_MAP = ImmutableMap.of(
        FlowRuleEntity.class, "flow",
        DegradeRuleEntity.class, "degrade",
        SystemRuleEntity.class, "system"
    );

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        ResolvableType resolvableType = ResolvableType.forClass(this.getClass());
        Class<?> clazz = resolvableType.getSuperType().getGeneric(0).resolve();
        String type = RULE_NAME_MAP.get(clazz);

        String rules = sentinelApiClient.fetchRules(ip, port, type);
        return rules;
    }
}
