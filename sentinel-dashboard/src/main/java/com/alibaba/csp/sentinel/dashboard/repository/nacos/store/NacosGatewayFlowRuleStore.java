
package com.alibaba.csp.sentinel.dashboard.repository.nacos.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;

/**
 *  类注释
 */

public class NacosGatewayFlowRuleStore extends AbstractDynamicRuleRepository<GatewayFlowRuleEntity> 
        implements DynamicRuleRepository<GatewayFlowRuleEntity>
{
    private DynamicRule<List<GatewayFlowRuleEntity>> ruleProvider;

    /**
     * 
     * @param ruleProvider
     * @param repository
     */
    public NacosGatewayFlowRuleStore(DynamicRule<List<GatewayFlowRuleEntity>> ruleProvider, RuleRepository<GatewayFlowRuleEntity, Long> repository)
    {
        this.ruleProvider = ruleProvider;
        this.repository = repository;
    }

    @Override
    public List<GatewayFlowRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<GatewayFlowRuleEntity> rules = ruleProvider.getRules(app);
        repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        try
        {
            List<GatewayFlowRuleEntity> rules = repository.findAllByApp(app);
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
