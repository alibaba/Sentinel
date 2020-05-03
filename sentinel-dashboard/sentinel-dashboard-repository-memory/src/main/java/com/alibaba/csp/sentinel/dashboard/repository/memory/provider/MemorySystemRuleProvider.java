package com.alibaba.csp.sentinel.dashboard.repository.memory.provider;

import com.alibaba.csp.sentinel.dashboard.entity.rule.SystemRuleEntity;
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
public class MemorySystemRuleProvider extends MemoryRuleProvider<SystemRuleEntity> {
}
