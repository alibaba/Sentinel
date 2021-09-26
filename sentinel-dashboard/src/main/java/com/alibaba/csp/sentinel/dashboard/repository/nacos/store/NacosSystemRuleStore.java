
package com.alibaba.csp.sentinel.dashboard.repository.nacos.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;

/**
 *  类注释
 * 
 */

public class NacosSystemRuleStore extends AbstractDynamicRuleRepository<SystemRuleEntity> implements DynamicRuleRepository<SystemRuleEntity>
{
    private DynamicRule<List<SystemRuleEntity>> ruleProvider;

    /**
     * 
     * @param ruleProvider
     * @param repository
     */
    public NacosSystemRuleStore(DynamicRule<List<SystemRuleEntity>> ruleProvider, RuleRepository<SystemRuleEntity, Long> repository)
    {
        this.ruleProvider = ruleProvider;
        this.repository = repository;
    }
    
    @Override
    public List<SystemRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<SystemRuleEntity> rules = ruleProvider.getRules(app);
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        try
        {
            List<SystemRuleEntity> rules = repository.findAllByApp(app);
            ruleProvider.publish(app, rules);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

}
