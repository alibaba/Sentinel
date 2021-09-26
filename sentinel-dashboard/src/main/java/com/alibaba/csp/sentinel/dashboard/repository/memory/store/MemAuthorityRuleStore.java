
package com.alibaba.csp.sentinel.dashboard.repository.memory.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;

/**
 *  类注释
 */

public class MemAuthorityRuleStore extends AbstractDynamicRuleRepository<AuthorityRuleEntity> implements DynamicRuleRepository<AuthorityRuleEntity>
{
    
    private SentinelApiClient sentinelApiClient;
    
    /**
     * 
     * @param sentinelApiClient
     * @param repository
     */    
    public MemAuthorityRuleStore(SentinelApiClient sentinelApiClient, RuleRepository<AuthorityRuleEntity, Long> repository)
    {
        this.sentinelApiClient = sentinelApiClient;
        this.repository = repository;
    }
    
    @Override
    public List<AuthorityRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<AuthorityRuleEntity> rules = sentinelApiClient.fetchAuthorityRulesOfMachine(app, ip, port);
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        List<AuthorityRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.setAuthorityRuleOfMachine(app, ip, port, rules);
    }

}
