
package com.alibaba.csp.sentinel.dashboard.repository.memory.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;

/**
 *  类注释
 * 
 */

public class MemGatewayFlowRuleStore extends AbstractDynamicRuleRepository<GatewayFlowRuleEntity> implements DynamicRuleRepository<GatewayFlowRuleEntity>
{
    private SentinelApiClient sentinelApiClient;
    
    /**
     * 
     * @param sentinelApiClient
     * @param repository
     */
    public MemGatewayFlowRuleStore(SentinelApiClient sentinelApiClient, RuleRepository<GatewayFlowRuleEntity, Long> repository)
    {
        this.sentinelApiClient = sentinelApiClient;
        this.repository = repository;
    }    
       

    @Override
    public List<GatewayFlowRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<GatewayFlowRuleEntity> rules = sentinelApiClient.fetchGatewayFlowRules(app, ip, port).get();
        repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        List<GatewayFlowRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.modifyGatewayFlowRules(app, ip, port, rules);
    }

}
