
package com.alibaba.csp.sentinel.dashboard.repository.nacos.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;

/**
 *  类注释
 * 
 */

public class NacosDegradeRuleStore extends AbstractDynamicRuleRepository<DegradeRuleEntity> implements DynamicRuleRepository<DegradeRuleEntity>
{
    
    private DynamicRule<List<DegradeRuleEntity>> ruleProvider;

    /**
     * 
     * @param ruleProvider
     * @param repository
     */
    public NacosDegradeRuleStore(DynamicRule<List<DegradeRuleEntity>> ruleProvider, RuleRepository<DegradeRuleEntity, Long> repository)
    {
        this.ruleProvider = ruleProvider;
        this.repository = repository;
    }
    
    @Override
    public List<DegradeRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<DegradeRuleEntity> rules = ruleProvider.getRules(app);        
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        try
        {
            List<DegradeRuleEntity> rules = repository.findAllByApp(app);
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
