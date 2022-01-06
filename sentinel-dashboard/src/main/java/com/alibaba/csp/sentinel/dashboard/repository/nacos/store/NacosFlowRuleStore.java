
package com.alibaba.csp.sentinel.dashboard.repository.nacos.store;

import java.util.List;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;

/**
 */
public class NacosFlowRuleStore extends AbstractDynamicRuleRepository<FlowRuleEntity> implements DynamicRuleRepository<FlowRuleEntity>
{

    private DynamicRule<List<FlowRuleEntity>> ruleProvider;

    /**
     * 
     * @param ruleProvider
     * @param repository
     */
    public NacosFlowRuleStore(DynamicRule<List<FlowRuleEntity>> ruleProvider, RuleRepository<FlowRuleEntity, Long> repository)
    {
        this.ruleProvider = ruleProvider;
        this.repository = repository;
    }

    @Override
    public List<FlowRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<FlowRuleEntity> rules = ruleProvider.getRules(app);
        if (rules != null && !rules.isEmpty()) {
            for (FlowRuleEntity entity : rules) {
                entity.setApp(app);
                if (entity.getClusterConfig() != null && entity.getClusterConfig().getFlowId() != null) {
                    entity.setId(entity.getClusterConfig().getFlowId());
                }
            }
        }
        rules = repository.saveAll(rules);
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        try
        {
            List<FlowRuleEntity> rules = repository.findAllByApp(app);
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
