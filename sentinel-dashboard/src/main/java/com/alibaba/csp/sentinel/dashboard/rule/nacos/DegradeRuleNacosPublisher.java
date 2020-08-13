package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DegradeRuleNacosPublisher  implements DynamicRulePublisher<List<DegradeRuleEntity>> {
    @Autowired
    private ConfigService configService;
    @Autowired
    private Converter<List<DegradeRuleEntity>, String> converter;

    @Override
    public void publish(String appId, List<DegradeRuleEntity> rules) throws Exception {
        AssertUtil.notEmpty(appId, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        configService.publishConfig(appId,NacosConfigUtil.DEGRADE_RULE, converter.convert(rules));
    }
}
