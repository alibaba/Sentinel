/*
 * 文件名称:          MemDegradeRuleStore.java
 * 版权所有(c) 2013——2025 无锡线上线下网络技术有限公司，保留所有权利
 * 创建时间:          2021年9月26日  下午1:02:20
 */
package com.alibaba.csp.sentinel.dashboard.repository.memory.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.AbstractDynamicRuleRepository;
import com.alibaba.csp.sentinel.dashboard.repository.store.DynamicRuleRepository;

/**
 *  类注释
 */

public class MemParamFlowRuleStore extends AbstractDynamicRuleRepository<ParamFlowRuleEntity> implements DynamicRuleRepository<ParamFlowRuleEntity>
{
    
    private SentinelApiClient sentinelApiClient;
    
    public MemParamFlowRuleStore(SentinelApiClient sentinelApiClient, RuleRepository<ParamFlowRuleEntity, Long> repository)
    {
        this.sentinelApiClient = sentinelApiClient;
        this.repository = repository;
    }    
    
    @Override
    public List<ParamFlowRuleEntity> queryRules(String app, String ip, Integer port) throws Throwable
    {
        List<ParamFlowRuleEntity> rules =  sentinelApiClient.fetchParamFlowRulesOfMachine(app, ip, port)
                .thenApply(repository::saveAll)
                .thenApply(Result::ofSuccess)
                .get().getData();
        return rules;
    }

    @Override
    public boolean publishRules(String app, String ip, Integer port)
    {
        List<ParamFlowRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        try
        {
            sentinelApiClient.setParamFlowRuleOfMachine(app, ip, port, rules).get();
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

}
