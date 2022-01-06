
package com.alibaba.csp.sentinel.dashboard.repository.memory.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;

/**
 *  类注释
 */

public class MemGatewayApiRuleStore extends AbstractDynamicRuleRepository<ApiDefinitionEntity> implements DynamicRuleRepository<ApiDefinitionEntity>
{
    private SentinelApiClient sentinelApiClient;
    
    /**
     * 
     * @param sentinelApiClient
     * @param repository
     */
    public MemGatewayApiRuleStore(SentinelApiClient sentinelApiClient, RuleRepository<ApiDefinitionEntity, Long> repository)
    {
        this.sentinelApiClient = sentinelApiClient;
        this.repository = repository;
    }    

    @Override
    public List<ApiDefinitionEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<ApiDefinitionEntity> apis = sentinelApiClient.fetchApis(app, ip, port).get();
        repository.saveAll(apis);
        return apis;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        List<ApiDefinitionEntity> apis = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.modifyApis(app, ip, port, apis);
    }
    

}
