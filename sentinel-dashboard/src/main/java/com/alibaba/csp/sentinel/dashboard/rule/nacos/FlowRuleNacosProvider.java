package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("flowRuleNacosProvider")
public class FlowRuleNacosProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {

    @Autowired
    private NacosConfig nacosConfig;

    private static final String DATA_ID_POSTFIX = "-flow-rules";
    private static final String GROUP_ID = "SENTINEL_GROUP";
    private static final long TIMEOUT = 3000;

    @Override
    public List<FlowRuleEntity> getRules(String appName) throws Exception {
        ConfigService configService = nacosConfig.getConfigService();
        String dataId = appName + DATA_ID_POSTFIX;
        String rules = configService.getConfig(dataId, GROUP_ID, TIMEOUT);
        if (rules == null || rules.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return JSON.parseArray(rules, FlowRuleEntity.class);
    }
}