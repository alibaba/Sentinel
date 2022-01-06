package com.alibaba.csp.sentinel.dashboard.repository.nacos.rule;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.NacosConfigProperties;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;

public class NacosSystemRuleDynamicRule implements DynamicRule<List<SystemRuleEntity>>
{

    private NacosConfigProperties nacosConfigProperties;
    
    private ConfigService configService;
    
    private Converter<String, List<SystemRuleEntity>> decoder;
    
    private Converter<List<SystemRuleEntity>, String> encoder;
    
    /**
     * 
     * @param nacosConfigProperties
     * @param configService
     * @param decoder
     * @param encoder
     */
    public NacosSystemRuleDynamicRule(NacosConfigProperties nacosConfigProperties, ConfigService configService,
            Converter<List<SystemRuleEntity>, String> encoder, Converter<String, List<SystemRuleEntity>> decoder)
    {
        this.nacosConfigProperties = nacosConfigProperties;
        this.configService = configService;
        this.decoder = decoder;
        this.encoder = encoder;
    }
    
    @Override
    public List<SystemRuleEntity> getRules(String appName) throws Exception
    {
        String rules = configService.getConfig(appName + nacosConfigProperties.getSystemSuffix(),
                nacosConfigProperties.getGroupId(), 3000);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return decoder.convert(rules);
    }

    @Override
    public void publish(String app, List<SystemRuleEntity> rules) throws Exception
    {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        configService.publishConfig(app + nacosConfigProperties.getSystemSuffix(),
                nacosConfigProperties.getGroupId(), encoder.convert(rules));
    }

}
