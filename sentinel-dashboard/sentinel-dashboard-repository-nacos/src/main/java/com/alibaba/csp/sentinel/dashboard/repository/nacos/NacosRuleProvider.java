package com.alibaba.csp.sentinel.dashboard.repository.nacos;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRuleProvider;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cdfive
 */
public class NacosRuleProvider<T extends RuleEntity> extends AbstractRuleProvider<T> {

    @Autowired
    private NacosProperties nacosProperties;

    @Autowired
    private ConfigService configService;

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        String ruleKey = buildRuleKey(app, ip, port);
        return configService.getConfig(ruleKey, nacosProperties.getSentinelGroup(), 3000);
    }
}
