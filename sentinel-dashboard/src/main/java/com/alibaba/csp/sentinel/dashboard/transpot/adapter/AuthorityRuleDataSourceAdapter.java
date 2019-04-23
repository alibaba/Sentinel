package com.alibaba.csp.sentinel.dashboard.transpot.adapter;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.management.DataSourceMachineInfo;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Third-party data source AuthorityRule common method adaptation
 *
 * @author longqiang
 */
@Component
public class AuthorityRuleDataSourceAdapter implements DataSourceAdapter<AuthorityRuleEntity> {

    @Override
    public String getKey(DataSourceMachineInfo dataSourceMachineInfo) {
        return dataSourceMachineInfo.getAuthorityRulesKey();
    }

    @Override
    public List<AuthorityRuleEntity> convert(String app, String ip, int port, String value) {
        return Optional.ofNullable(value)
                        .map(rules -> JSON.parseArray(rules, AuthorityRule.class))
                        .map(rules -> rules.stream()
                                            .map(e -> AuthorityRuleEntity.fromAuthorityRule(app, ip, port, e))
                                            .collect(Collectors.toList()))
                        .orElse(null);
    }

    @Override
    public String processRules(List<AuthorityRuleEntity> rules) {
        return JSON.toJSONString(rules.stream().map(AuthorityRuleEntity::toRule).collect(Collectors.toList()));
    }
}
