package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.rule.RuleConfigService;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

public class NacosConfigService implements RuleConfigService {

    ConfigService configService;

    public NacosConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public String getConfig(String dataId, String group) {
        try {
            return configService.getConfig(dataId,group,3000);
        } catch (NacosException e) {
            return null;
        }
    }

    @Override
    public void publishConfig(String dataId, String group, String content) {
        try {
            configService.publishConfig(dataId,group,content);
        } catch (NacosException e) {
        }
    }
}
