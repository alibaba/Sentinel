package com.alibaba.csp.sentinel.dashboard.fetch.inmemory;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.google.common.collect.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Obtaining param flow rules from Target Machines by http client
 *
 * @author longqiang
 */
@Component
@ConditionalOnProperty(name = "ruleDataSource", havingValue = "inMemory", matchIfMissing = true)
public class InMemoryParamFlowRuleFetcher extends InMemoryFetchAdapter<ParamFlowRuleEntity> {

    public InMemoryParamFlowRuleFetcher(SentinelApiClient sentinelApiClient) {
        super(sentinelApiClient);
    }

    @Override
    public List<ParamFlowRuleEntity> fetch(String app, String ip, int port) {
        try {
            return sentinelApiClient.fetchParamFlowRulesOfMachine(app, ip, port).get();
        } catch (InterruptedException e) {
            RecordLog.warn("[InMemoryParamFlowRulePublisher] Error when publish with InterruptedException ", e);
        } catch (ExecutionException e) {
            RecordLog.warn("[InMemoryParamFlowRulePublisher] Error when publish with ExecutionException ", e);
        }
        return Lists.newArrayList();
    }
}
