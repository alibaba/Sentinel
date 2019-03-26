package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.ApolloMachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.fetch.Fetcher;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * If the configuration modification is not triggered by sentinel-dashboard, need to notify this
 *
 * @author longqiang
 */
@RestController
@RequestMapping(value = "apolloConfigChange", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApolloConfigChangeListener {

    private final Logger logger = LoggerFactory.getLogger(ApolloConfigChangeListener.class);

    @Autowired
    private AppManagement appManagement;

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
    public boolean flowRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<FlowRuleEntity> rules = flowRuleFetcher.fetch(app, ip, port);
            flowRuleRepository.saveAll(rules);
        }
        return checkResult;
    }

    @PutMapping("degradeRules")
    public boolean degradeRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<DegradeRuleEntity> rules = degradeRuleFetcher.fetch(app, ip, port);
            degradeRuleRepository.saveAll(rules);
        }
        return checkResult;
    }

    @PutMapping("authorityRules")
    public boolean authorityRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<AuthorityRuleEntity> rules = authorityRuleFetcher.fetch(app, ip, port);
            authorityRuleRepository.saveAll(rules);
        }
        return checkResult;
    }

    @PutMapping("systemRules")
    public boolean systemRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<SystemRuleEntity> rules = systemRuleFetcher.fetch(app, ip, port);
            systemRuleRepository.saveAll(rules);
        }
        return checkResult;
    }

    @PutMapping("paramFlowRules")
    public boolean paramFlowRulesOnChange(String operator, String app, String ip, Integer port) {
        boolean checkResult = checkOperator(operator, app, ip, port);
        if (checkResult) {
            List<ParamFlowRuleEntity> rules = paramFlowRuleFetcher.fetch(app, ip, port);
            paramFlowRuleRepository.saveAll(rules);
        }
        return checkResult;
    }

    private boolean checkOperator(String operator, String app, String ip, Integer port) {
        Optional<MachineInfo> machineInfoOptional = appManagement.getDetailApp(app).getMachine(ip, port);
        ApolloMachineInfo apolloMachineInfo = (ApolloMachineInfo) machineInfoOptional.get();
        boolean result = true;
        if (operator.equals(apolloMachineInfo.getOperator())) {
            logger.info("it is sentinel dashboard modifications, no synchronization required, record operator:{}, request operator:{}",
                        apolloMachineInfo.getOperator(), operator);
            result = false;
        }
        return result;
    }

}
