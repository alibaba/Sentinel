
package com.alibaba.csp.sentinel.dashboard.repository.nacos;

import java.util.Properties;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.rule.NacosAuthorityDynamicRule;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.rule.NacosDegradeDynamicRule;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.rule.NacosFlowRuleDynamicRule;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.rule.NacosGatewayApiDynamicRule;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.rule.NacosGatewayRuleDynamicRule;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.rule.NacosParamRuleDynamicRule;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.rule.NacosSystemRuleDynamicRule;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.store.NacosAuthorityRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.store.NacosDegradeRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.store.NacosFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.store.NacosGatewayApiRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.store.NacosGatewayFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.store.NacosParamFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.nacos.store.NacosSystemRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.RepositoryType;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(prefix = "rule.repository", value = "type", havingValue = RepositoryType.NACOS, matchIfMissing = false)
@EnableConfigurationProperties({NacosConfigProperties.class})
public class NacosStoreConfig {
    
	@Autowired
    private NacosConfigProperties nacosConfigProperties;	
    
    @Bean
    public ConfigService nacosConfigService() throws Exception {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosConfigProperties.getServerAddr());
        if (nacosConfigProperties.getNamespace() != null)
        {
            properties.put(PropertyKeyConst.NAMESPACE, nacosConfigProperties.getNamespace());
        }
        return ConfigFactory.createConfigService(properties);
    }
    
    @Bean
    public NacosAuthorityRuleStore createNacosAuthorityRuleStore(ConfigService configService, RuleRepository<AuthorityRuleEntity, Long> repository)
    {
        NacosAuthorityDynamicRule store = new NacosAuthorityDynamicRule(nacosConfigProperties, configService, 
                JSON::toJSONString, s -> JSON.parseArray(s, AuthorityRuleEntity.class));
        NacosAuthorityRuleStore rule = new NacosAuthorityRuleStore(store,  repository);
        
        return rule;
    }
        
    @Bean
    public NacosDegradeRuleStore createNacosDegradeRuleStore(ConfigService configService, RuleRepository<DegradeRuleEntity, Long> repository)
    {
        NacosDegradeDynamicRule store = new NacosDegradeDynamicRule(nacosConfigProperties, configService, 
                JSON::toJSONString, s -> JSON.parseArray(s, DegradeRuleEntity.class));
        NacosDegradeRuleStore rule = new NacosDegradeRuleStore(store,  repository);
        
        return rule;
    }

    @Bean
    public NacosFlowRuleStore createNacosFlowRuleStore(ConfigService configService, RuleRepository<FlowRuleEntity, Long> repository)
    {
        NacosFlowRuleDynamicRule store = new NacosFlowRuleDynamicRule(nacosConfigProperties, configService, 
                JSON::toJSONString, s -> JSON.parseArray(s, FlowRuleEntity.class));
        NacosFlowRuleStore rule = new NacosFlowRuleStore(store,  repository);
        
        return rule;
    }

    @Bean
    public NacosGatewayFlowRuleStore createNacosGatewayFlowRuleStore(ConfigService configService, RuleRepository<GatewayFlowRuleEntity, Long> repository)
    {
        NacosGatewayRuleDynamicRule store = new NacosGatewayRuleDynamicRule(nacosConfigProperties, configService, 
                JSON::toJSONString, s -> JSON.parseArray(s, GatewayFlowRuleEntity.class));
        NacosGatewayFlowRuleStore rule = new NacosGatewayFlowRuleStore(store,  repository);
        
        return rule;
    }
    
    @Bean
    public NacosGatewayApiRuleStore createNacosGatewayApiRuleStore(ConfigService configService, RuleRepository<ApiDefinitionEntity, Long> repository)
    {
        NacosGatewayApiDynamicRule store = new NacosGatewayApiDynamicRule(nacosConfigProperties, configService, 
                JSON::toJSONString, s -> JSON.parseArray(s, ApiDefinitionEntity.class));
        NacosGatewayApiRuleStore rule = new NacosGatewayApiRuleStore(store,  repository);
        
        return rule;
    }    
    
    @Bean
    public NacosParamFlowRuleStore createNacosParamFlowRuleStore(ConfigService configService, RuleRepository<ParamFlowRuleEntity, Long> repository)
    {
        NacosParamRuleDynamicRule store = new NacosParamRuleDynamicRule(nacosConfigProperties, configService, 
                JSON::toJSONString, s -> JSON.parseArray(s, ParamFlowRuleEntity.class));
        NacosParamFlowRuleStore rule = new NacosParamFlowRuleStore(store,  repository);
        
        return rule;
    }

    @Bean
    public NacosSystemRuleStore createNacosSystemRuleStore(ConfigService configService, RuleRepository<SystemRuleEntity, Long> repository)
    {
        NacosSystemRuleDynamicRule store = new NacosSystemRuleDynamicRule(nacosConfigProperties, configService, 
                JSON::toJSONString, s -> JSON.parseArray(s, SystemRuleEntity.class));
        NacosSystemRuleStore rule = new NacosSystemRuleStore(store,  repository);
        
        return rule;
    }
    
    
    
}
