package com.alibaba.csp.sentinel.dashboard.controller.config;

import com.alibaba.csp.sentinel.dashboard.controller.config.check.Checker;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.fetch.Fetcher;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    private Fetcher<FlowRuleEntity> flowRuleFetcher;

    @Autowired
    private Fetcher<DegradeRuleEntity> degradeRuleFetcher;

    @Autowired
    private Fetcher<AuthorityRuleEntity> authorityRuleFetcher;

    @Autowired
    private Fetcher<ParamFlowRuleEntity> paramFlowRuleFetcher;

    @Autowired
    private Fetcher<SystemRuleEntity> systemRuleFetcher;

    @Autowired
    private InMemoryRuleRepositoryAdapter<FlowRuleEntity> flowRuleRepository;

    @Autowired
    private InMemoryRuleRepositoryAdapter<DegradeRuleEntity> degradeRuleRepository;

    @Autowired
    private InMemoryRuleRepositoryAdapter<AuthorityRuleEntity> authorityRuleRepository;

    @Autowired
    private InMemoryRuleRepositoryAdapter<ParamFlowRuleEntity> paramFlowRuleRepository;

    @Autowired
    private InMemoryRuleRepositoryAdapter<SystemRuleEntity> systemRuleRepository;

    @PutMapping("flowRules")
    public Result<Boolean> flowRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checker.checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<FlowRuleEntity> rules = flowRuleFetcher.fetch(app, ip, port);
            flowRuleRepository.saveAll(rules);
        }
        return Result.ofSuccess(checkResult);
    }

    @PutMapping("degradeRules")
    public Result<Boolean> degradeRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checker.checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<DegradeRuleEntity> rules = degradeRuleFetcher.fetch(app, ip, port);
            degradeRuleRepository.saveAll(rules);
        }
        return Result.ofSuccess(checkResult);
    }

    @PutMapping("authorityRules")
    public Result<Boolean> authorityRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checker.checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<AuthorityRuleEntity> rules = authorityRuleFetcher.fetch(app, ip, port);
            authorityRuleRepository.saveAll(rules);
        }
        return Result.ofSuccess(checkResult);
    }

    @PutMapping("systemRules")
    public Result<Boolean> systemRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checker.checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<SystemRuleEntity> rules = systemRuleFetcher.fetch(app, ip, port);
            systemRuleRepository.saveAll(rules);
        }
        return Result.ofSuccess(checkResult);
    }

    @PutMapping("paramFlowRules")
    public Result<Boolean> paramFlowRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checker.checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<ParamFlowRuleEntity> rules = paramFlowRuleFetcher.fetch(app, ip, port);
            paramFlowRuleRepository.saveAll(rules);
        }
        return Result.ofSuccess(checkResult);
    }

}
