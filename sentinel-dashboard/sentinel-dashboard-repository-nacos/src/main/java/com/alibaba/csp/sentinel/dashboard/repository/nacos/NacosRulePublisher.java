package com.alibaba.csp.sentinel.dashboard.repository.nacos;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRulePublisher;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cdfive
 */
public class NacosRulePublisher<T extends RuleEntity> extends AbstractRulePublisher<T> {

    @Autowired
    private NacosProperties nacosProperties;

    @Autowired
    private ConfigService configService;

    @Override
    protected void publishRules(String app, String ip, Integer port, String rules) throws Exception {
        String ruleKey = buildRuleKey(app, ip, port);
        boolean result = configService.publishConfig(ruleKey, nacosProperties.getSentinelGroup(), rules);
        if (!result) {
            // TODO
        }
    }
}
