
package com.alibaba.csp.sentinel.dashboard.repository.memory.store;

import java.util.List;


import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;

/**
 *  类注释
 * 
 */

public class MemDegradeRuleStore extends AbstractDynamicRuleRepository<DegradeRuleEntity> implements DynamicRuleRepository<DegradeRuleEntity>
{
    
    private SentinelApiClient sentinelApiClient;
    
    public MemDegradeRuleStore(SentinelApiClient sentinelApiClient, RuleRepository<DegradeRuleEntity, Long> repository)
    {
        this.sentinelApiClient = sentinelApiClient;
        this.repository = repository;
    }
    
    @Override
    public List<DegradeRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<DegradeRuleEntity> rules = sentinelApiClient.fetchDegradeRuleOfMachine(app, ip, port);
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        List<DegradeRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.setDegradeRuleOfMachine(app, ip, port, rules);
    }

}
