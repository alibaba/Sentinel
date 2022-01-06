package com.alibaba.csp.sentinel.dashboard.repository.nacos.rule;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.NacosConfigProperties;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;

public class NacosGatewayApiDynamicRule implements DynamicRule<List<ApiDefinitionEntity>>
{

    private NacosConfigProperties nacosConfigProperties;
    
    private ConfigService configService;
    
    private Converter<String, List<ApiDefinitionEntity>> decoder;
    
    private Converter<List<ApiDefinitionEntity>, String> encoder;
    
    /**
     * 
     * @param nacosConfigProperties
     * @param configService
     * @param decoder
     * @param encoder
     */
    public NacosGatewayApiDynamicRule(NacosConfigProperties nacosConfigProperties, ConfigService configService,
            Converter<List<ApiDefinitionEntity>, String> encoder, Converter<String, List<ApiDefinitionEntity>> decoder)
    {
        this.nacosConfigProperties = nacosConfigProperties;
        this.configService = configService;
        this.decoder = decoder;
        this.encoder = encoder;
    }
    
    @Override
    public List<ApiDefinitionEntity> getRules(String appName) throws Exception
    {
        String rules = configService.getConfig(appName + nacosConfigProperties.getGatewayApiSuffix(),
                nacosConfigProperties.getGroupId(), 3000);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return decoder.convert(rules);
    }

    @Override
    public void publish(String app, List<ApiDefinitionEntity> rules) throws Exception
    {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        configService.publishConfig(app + nacosConfigProperties.getGatewayApiSuffix(),
                nacosConfigProperties.getGroupId(), encoder.convert(rules));
    }

}
