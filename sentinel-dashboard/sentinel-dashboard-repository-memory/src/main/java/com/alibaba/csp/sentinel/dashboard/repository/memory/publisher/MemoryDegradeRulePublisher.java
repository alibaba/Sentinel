package com.alibaba.csp.sentinel.dashboard.repository.memory.publisher;

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.memory.MemoryConfig;
import com.alibaba.csp.sentinel.dashboard.repository.memory.MemoryRulePublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
//@ConditionalOnMissingBean
@ConditionalOnBean(MemoryConfig.class)
@Component
public class MemoryDegradeRulePublisher extends MemoryRulePublisher<DegradeRuleEntity> {

}
