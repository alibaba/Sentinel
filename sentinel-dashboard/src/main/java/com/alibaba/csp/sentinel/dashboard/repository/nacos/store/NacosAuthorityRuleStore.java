
package com.alibaba.csp.sentinel.dashboard.repository.nacos.store;

import java.util.List;


import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;

/**
 *  类注释
 * 
 * @since     3.0
 */

public class NacosAuthorityRuleStore extends AbstractDynamicRuleRepository<AuthorityRuleEntity> implements DynamicRuleRepository<AuthorityRuleEntity>
{
    
    private DynamicRule<List<AuthorityRuleEntity>> ruleProvider;
    
    /**
     * 
     * @param ruleProvider
     * @param repository
     */
    public NacosAuthorityRuleStore(DynamicRule<List<AuthorityRuleEntity>> ruleProvider, RuleRepository<AuthorityRuleEntity, Long> repository)
    {
        this.ruleProvider = ruleProvider;
        this.repository = repository;
    }

    @Override
    public List<AuthorityRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<AuthorityRuleEntity> rules = ruleProvider.getRules(app);
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        try
        {
            List<AuthorityRuleEntity> rules = repository.findAllByApp(app);
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
