package com.alibaba.csp.sentinel.dashboard.repository.memory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.memory.store.MemAuthorityRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.memory.store.MemDegradeRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.memory.store.MemFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.memory.store.MemGatewayApiRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.memory.store.MemGatewayFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.memory.store.MemParamFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.memory.store.MemSystemRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.RepositoryType;

/**
 * 
 *  类注释
 */

@Configuration
@ConditionalOnProperty(prefix = "rule.repository", value = "type", havingValue = RepositoryType.DEFAULT, matchIfMissing = false)
public class MemoryStoreConfig
{
    @Autowired
    private SentinelApiClient sentinelApiClient;
    
    @Bean
    public MemAuthorityRuleStore createMemAuthorityRuleStore(RuleRepository<AuthorityRuleEntity, Long> repository)
    {
        return new MemAuthorityRuleStore(sentinelApiClient, repository);
    }
    
    @Bean
    public MemDegradeRuleStore createMemDegradeRuleStore(RuleRepository<DegradeRuleEntity, Long> repository)
    {
        return new MemDegradeRuleStore(sentinelApiClient, repository);
    }
    
    @Bean
    public MemFlowRuleStore createMemFlowRuleStore(RuleRepository<FlowRuleEntity, Long> repository)
    {
        return new MemFlowRuleStore(sentinelApiClient, repository);
    }
    
    @Bean
    public MemGatewayApiRuleStore createMemGatewayApiRuleStore(RuleRepository<ApiDefinitionEntity, Long> repository)
    {
        return new MemGatewayApiRuleStore(sentinelApiClient, repository);
    }
    
    @Bean
    public MemGatewayFlowRuleStore createMemGatewayFlowRuleStore(RuleRepository<GatewayFlowRuleEntity, Long> repository)
    {
        return new MemGatewayFlowRuleStore(sentinelApiClient, repository);
    }
    
    @Bean
    public MemParamFlowRuleStore createMemParamFlowRuleStore(RuleRepository<ParamFlowRuleEntity, Long> repository)
    {
        return new MemParamFlowRuleStore(sentinelApiClient, repository);
    }
    
    @Bean
    public MemSystemRuleStore createMemSystemRuleStore(RuleRepository<SystemRuleEntity, Long> repository)
    {
        return new MemSystemRuleStore(sentinelApiClient, repository);
    }
}
