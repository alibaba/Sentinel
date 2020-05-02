package com.alibaba.csp.sentinel.dashboard.repository.memory;

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRulePublisher;
import com.alibaba.csp.sentinel.transport.client.SentinelTransportClient;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import java.util.Map;

/**
 * @author cdfive
 */
public class MemoryRulePublisher<T extends RuleEntity> extends AbstractRulePublisher<T> {

    @Autowired
    private SentinelTransportClient sentinelTransportClient;

    private final static Map<Class, String> RULE_NAME_MAP = ImmutableMap.of(
        FlowRuleEntity.class, "flow",
        DegradeRuleEntity.class, "degrade",
        SystemRuleEntity.class, "system"
    );

    @Override
    protected void publishRules(String app, String ip, Integer port, String rules) throws Exception {
        ResolvableType resolvableType = ResolvableType.forClass(this.getClass());
        Class<?> clazz = resolvableType.getSuperType().getGeneric(0).resolve();
        String type = RULE_NAME_MAP.get(clazz);

        sentinelTransportClient.setRules(ip, port, type, rules);
    }
}
