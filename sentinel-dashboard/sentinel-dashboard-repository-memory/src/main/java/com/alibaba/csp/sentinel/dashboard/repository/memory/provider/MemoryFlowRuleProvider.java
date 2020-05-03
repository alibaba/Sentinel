package com.alibaba.csp.sentinel.dashboard.repository.memory.provider;

import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.memory.MemoryConfig;
import com.alibaba.csp.sentinel.dashboard.repository.memory.MemoryRuleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
//@ConditionalOnMissingBean
@ConditionalOnBean(MemoryConfig.class)
@Component
public class MemoryFlowRuleProvider extends MemoryRuleProvider<FlowRuleEntity> {

    public MemoryFlowRuleProvider() {
        System.out.println("MemoryFlowRuleProvider");
    }
}
