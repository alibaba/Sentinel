package com.alibaba.csp.sentinel.dashboard.rule.type.nacos.provider;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.type.nacos.NacosConfig;
import com.alibaba.csp.sentinel.dashboard.rule.type.nacos.NacosRuleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(NacosConfig.class)
@Component
public class NacosDegradeRuleProvider extends NacosRuleProvider<DegradeRuleEntity> {

}
