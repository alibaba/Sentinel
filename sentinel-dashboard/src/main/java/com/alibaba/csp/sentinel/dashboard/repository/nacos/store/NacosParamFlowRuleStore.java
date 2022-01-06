
package com.alibaba.csp.sentinel.dashboard.repository.nacos.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;

/**
 *  类注释
 */

public class NacosParamFlowRuleStore extends AbstractDynamicRuleRepository<ParamFlowRuleEntity> implements DynamicRuleRepository<ParamFlowRuleEntity>
{
    private DynamicRule<List<ParamFlowRuleEntity>> ruleProvider;

    /**
     * 
     * @param ruleProvider
     * @param repository
     */
    public NacosParamFlowRuleStore(DynamicRule<List<ParamFlowRuleEntity>> ruleProvider, RuleRepository<ParamFlowRuleEntity, Long> repository)
    {
        this.ruleProvider = ruleProvider;
        this.repository = repository;
    }

    @Override
    public List<ParamFlowRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<ParamFlowRuleEntity> rules = ruleProvider.getRules(app);
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        try
        {
            List<ParamFlowRuleEntity> rules = repository.findAllByApp(app);
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
