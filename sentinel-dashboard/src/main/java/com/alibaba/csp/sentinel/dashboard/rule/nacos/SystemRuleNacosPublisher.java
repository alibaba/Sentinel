package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;

import java.util.List;

public class SystemRuleNacosPublisher extends AbstractDynamicRulePublisher<List<SystemRuleEntity>> {
    public SystemRuleNacosPublisher(Converter<List<SystemRuleEntity>,String> converter){
        super(NacosConfigUtil.SYSTEM_RULE,converter);
    }
}
