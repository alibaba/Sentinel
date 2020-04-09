package com.alibaba.csp.sentinel.dashboard.rule.type.nacos.publisher;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.type.nacos.NacosConfig;
import com.alibaba.csp.sentinel.dashboard.rule.type.nacos.NacosRulePublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(NacosConfig.class)
@Component
public class NacosDegradeRulePublisher extends NacosRulePublisher<DegradeRuleEntity> {

}
