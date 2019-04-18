package com.alibaba.csp.sentinel.dashboard.transpot.publish.nacos;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * publish system rule by apollo
 *
 * @author longqiang
 */
@Component(Constants.SYSTEM_RULE_PUBLISHER)
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "nacos")
public class NacosSystemRulePublisher extends NacosPublishAdapter<SystemRuleEntity> {
}
