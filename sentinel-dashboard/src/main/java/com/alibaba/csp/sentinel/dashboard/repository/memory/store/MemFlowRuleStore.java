
package com.alibaba.csp.sentinel.dashboard.repository.memory.store;

import java.util.List;
import java.util.concurrent.TimeUnit;


import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;

/**
 *  类注释
 * 
 */

public class MemFlowRuleStore extends AbstractDynamicRuleRepository<FlowRuleEntity> implements DynamicRuleRepository<FlowRuleEntity>
{
    
    private SentinelApiClient sentinelApiClient;
    
    /**
     * 
     * @param sentinelApiClient
     * @param repository
     */
    public MemFlowRuleStore(SentinelApiClient sentinelApiClient, RuleRepository<FlowRuleEntity, Long> repository)
    {
        this.sentinelApiClient = sentinelApiClient;
        this.repository = repository;
    }
    
    @Override
    public List<FlowRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<FlowRuleEntity> rules = sentinelApiClient.fetchFlowRuleOfMachine(app, ip, port);
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        List<FlowRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        try
        {
            sentinelApiClient.setFlowRuleOfMachineAsync(app, ip, port, rules).get(5000, TimeUnit.MILLISECONDS);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

}
