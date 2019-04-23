package com.alibaba.csp.sentinel.dashboard.transpot.adapter;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.management.DataSourceMachineInfo;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Third-party data source FlowRule common method adaptation
 *
 * @author longqiang
 */
@Component
public class FlowRuleDataSourceAdapter implements DataSourceAdapter<FlowRuleEntity> {

    @Override
    public String getKey(DataSourceMachineInfo dataSourceMachineInfo) {
        return dataSourceMachineInfo.getFlowRulesKey();
    }

    @Override
    public List<FlowRuleEntity> convert(String app, String ip, int port, String value) {
        return Optional.ofNullable(value)
                        .map(rules -> JSON.parseArray(rules, FlowRule.class))
                        .map(rules -> rules.stream()
                                            .map(e -> FlowRuleEntity.fromFlowRule(app, ip, port, e))
                                            .collect(Collectors.toList()))
                        .orElse(null);
    }

    @Override
    public String processRules(List<FlowRuleEntity> rules) {
        return JSON.toJSONString(rules.stream().map(FlowRuleEntity::toRule).collect(Collectors.toList()));
    }
}
