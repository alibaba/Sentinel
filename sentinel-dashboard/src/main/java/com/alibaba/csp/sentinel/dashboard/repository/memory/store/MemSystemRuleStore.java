
package com.alibaba.csp.sentinel.dashboard.repository.memory.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;

/**
 *  类注释
 * 
 */

public class MemSystemRuleStore extends AbstractDynamicRuleRepository<SystemRuleEntity> implements DynamicRuleRepository<SystemRuleEntity>
{
    
    private SentinelApiClient sentinelApiClient;
    
    /**
     * 
     * @param sentinelApiClient
     * @param repository
     */
    public MemSystemRuleStore(SentinelApiClient sentinelApiClient, RuleRepository<SystemRuleEntity, Long> repository)
    {
        this.sentinelApiClient = sentinelApiClient;
        this.repository = repository;
    }    
            
    @Override
    public List<SystemRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<SystemRuleEntity> rules = sentinelApiClient.fetchSystemRuleOfMachine(app, ip, port);
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        List<SystemRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.setSystemRuleOfMachine(app, ip, port, rules);
    }

}
