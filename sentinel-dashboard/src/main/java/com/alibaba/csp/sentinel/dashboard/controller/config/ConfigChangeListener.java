package com.alibaba.csp.sentinel.dashboard.controller.config;

import com.alibaba.csp.sentinel.dashboard.Constants;
import com.alibaba.csp.sentinel.dashboard.controller.config.check.Checker;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.transpot.fetch.Fetcher;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * If the configuration modification is not triggered by sentinel-dashboard, need to notify this
 *
 * @author longqiang
 */
@RestController
@RequestMapping(value = "configChange", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConfigChangeListener {

    @Autowired
    private Checker checker;

    @Autowired
    private Map<String, InMemoryRuleRepositoryAdapter> repositoryMap;

    @Autowired
    private Map<String, Fetcher> fetcherMap;

    @PutMapping("flowRules")
    public Result<Boolean> flowRulesOnChange(String operator, String app, String ip, Integer port) {
        return Result.ofSuccess(processOnChange(operator, app, ip, port, Constants.FLOW_RULE_FETCHER, Constants.FLOW_RULE_STORE));
    }

    @PutMapping("degradeRules")
    public Result<Boolean> degradeRulesOnChange(String operator, String app, String ip, Integer port) {
        return Result.ofSuccess(processOnChange(operator, app, ip, port, Constants.DEGRADE_RULE_FETCHER, Constants.DEGRADE_RULE_STORE));
    }

    @PutMapping("authorityRules")
    public Result<Boolean> authorityRulesOnChange(String operator, String app, String ip, Integer port) {
        return Result.ofSuccess(processOnChange(operator, app, ip, port, Constants.AUTHORITY_RULE_FETCHER, Constants.AUTHORITY_RULE_STORE));
    }

    @PutMapping("systemRules")
    public Result<Boolean> systemRulesOnChange(String operator, String app, String ip, Integer port) {
        return Result.ofSuccess(processOnChange(operator, app, ip, port, Constants.SYSTEM_RULE_FETCHER, Constants.SYSTEM_RULE_STORE));
    }

    @PutMapping("paramFlowRules")
    public Result<Boolean> paramFlowRulesOnChange(String operator, String app, String ip, Integer port) {
        return Result.ofSuccess(processOnChange(operator, app, ip, port, Constants.PARAM_FLOW_RULE_FETCHER, Constants.PARAM_FLOW_RULE_STORE));
    }

    private Boolean processOnChange(String operator, String app, String ip, Integer port, String fetcherName, String repositoryName) {
        boolean checkResult = checker.checkOperator(operator, app, ip, port);
        if (checkResult) {
            Fetcher fetcher = fetcherMap.get(fetcherName);
            InMemoryRuleRepositoryAdapter repository = repositoryMap.get(repositoryName);
            List<RuleEntity> rules = fetcher.fetch(app, ip, port);
            repository.deleteByMachine(MachineInfo.of(app, ip, port));
            repository.saveAll(rules);
        }
        return checkResult;
    }

}
