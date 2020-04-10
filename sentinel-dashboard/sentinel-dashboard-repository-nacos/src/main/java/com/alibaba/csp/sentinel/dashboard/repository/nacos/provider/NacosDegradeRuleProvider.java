package com.alibaba.csp.sentinel.dashboard.repository.nacos.provider;

import com.alibaba.csp.sentinel.dashboard.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.NacosConfig;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.NacosRuleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(NacosConfig.class)
@Component
public class NacosDegradeRuleProvider extends NacosRuleProvider<DegradeRuleEntity> {

}
