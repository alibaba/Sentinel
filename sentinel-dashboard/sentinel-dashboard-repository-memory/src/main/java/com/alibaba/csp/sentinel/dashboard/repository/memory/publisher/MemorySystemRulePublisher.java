package com.alibaba.csp.sentinel.dashboard.repository.memory.publisher;

import com.alibaba.csp.sentinel.dashboard.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.memory.MemoryConfig;
import com.alibaba.csp.sentinel.dashboard.repository.memory.MemoryRulePublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(MemoryConfig.class)
@Component
public class MemorySystemRulePublisher extends MemoryRulePublisher<SystemRuleEntity> {

}
