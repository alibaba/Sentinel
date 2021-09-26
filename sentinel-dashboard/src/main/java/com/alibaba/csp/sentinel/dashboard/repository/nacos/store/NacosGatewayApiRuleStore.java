
package com.alibaba.csp.sentinel.dashboard.repository.nacos.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRule;

/**
 *  类注释
 */

public class NacosGatewayApiRuleStore extends AbstractDynamicRuleRepository<ApiDefinitionEntity> implements DynamicRuleRepository<ApiDefinitionEntity>
{

    private DynamicRule<List<ApiDefinitionEntity>> ruleProvider;

    /**
     * 
     * @param ruleProvider
     * @param repository
     */
    public NacosGatewayApiRuleStore(DynamicRule<List<ApiDefinitionEntity>> ruleProvider, RuleRepository<ApiDefinitionEntity, Long> repository)
    {
        this.ruleProvider = ruleProvider;
        this.repository = repository;
    }
    
    @Override
    public List<ApiDefinitionEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<ApiDefinitionEntity> apis = ruleProvider.getRules(app);
        repository.saveAll(apis);
        return apis;
    }
    
    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        try
        {
            List<ApiDefinitionEntity> apis = repository.findAllByApp(app);
            ruleProvider.publish(app, apis);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
}
