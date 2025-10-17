package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("systemRuleNacosPublisher")
public class SystemRuleNacosPublisher implements DynamicRulePublisher<List<SystemRuleEntity>> {

    @Autowired
    private NacosConfig nacosConfig;

    private static final String DATA_ID_POSTFIX = "-system-rules";
    private static final String GROUP_ID = "SENTINEL_GROUP";

    @Override
    public void publish(String app, List<SystemRuleEntity> rules) throws Exception {
        ConfigService configService = nacosConfig.getConfigService();
        String dataId = app + DATA_ID_POSTFIX;
        String rulesStr = (rules == null || rules.isEmpty()) ? "[]" : JSON.toJSONString(rules);
        configService.publishConfig(dataId, GROUP_ID, rulesStr);
    }
}