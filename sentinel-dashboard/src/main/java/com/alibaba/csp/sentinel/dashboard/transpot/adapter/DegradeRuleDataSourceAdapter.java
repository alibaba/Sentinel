package com.alibaba.csp.sentinel.dashboard.transpot.adapter;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.management.DataSourceMachineInfo;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Third-party data source DegradeRule common method adaptation
 *
 * @author longqiang
 */
@Component
public class DegradeRuleDataSourceAdapter implements DataSourceAdapter<DegradeRuleEntity> {

    @Override
    public String getKey(DataSourceMachineInfo dataSourceMachineInfo) {
        return dataSourceMachineInfo.getDegradeRulesKey();
    }

    @Override
    public List<DegradeRuleEntity> convert(String app, String ip, int port, String value) {
        return Optional.ofNullable(value)
                        .map(rules -> JSON.parseArray(rules, DegradeRule.class))
                        .map(rules -> rules.stream()
                                            .map(e -> DegradeRuleEntity.fromDegradeRule(app, ip, port, e))
                                            .collect(Collectors.toList()))
                        .orElse(null);
    }

    @Override
    public String processRules(List<DegradeRuleEntity> rules) {
        return JSON.toJSONString(rules.stream().map(DegradeRuleEntity::toRule).collect(Collectors.toList()));
    }
}
