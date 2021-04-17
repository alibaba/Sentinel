/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.service;

import com.alibaba.csp.sentinel.dashboard.converter.RuleConverter;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.RuleTypeEnum;
import com.alibaba.csp.sentinel.slots.block.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static com.alibaba.csp.sentinel.dashboard.util.ConfigFileUtils.writeAsZipOutputStream;

/**
 * @author wxq
 */
@Service
public class ProjectConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectConfigService.class);

    @Autowired
    private RuleRepository<AuthorityRuleEntity, Long> authorityRuleEntityLongRuleRepository;

    @Autowired
    private RuleRepository<SystemRuleEntity, Long> systemRuleEntityLongRuleRepository;

    @Autowired
    private RuleRepository<DegradeRuleEntity, Long> degradeRuleEntityLongRuleRepository;

    @Autowired
    private RuleRepository<ParamFlowRuleEntity, Long> paramFlowRuleEntityLongRuleRepository;

    @Autowired
    private RuleRepository<FlowRuleEntity, Long> flowRuleEntityLongRuleRepository;

    @Autowired
    private RuleRepository<ApiDefinitionEntity, Long> apiDefinitionEntityLongRuleRepository;

    @Autowired
    private RuleRepository<GatewayFlowRuleEntity, Long> gatewayFlowRuleEntityLongRuleRepository;

    @Autowired
    private AppManagement appManagement;

    private Map<RuleTypeEnum, List<? extends Rule>> getRules(String projectName) {
        Map<RuleTypeEnum, List<? extends Rule>> ruleTypeEnumListMap = new HashMap<>();
        for (RuleTypeEnum ruleTypeEnum : RuleTypeEnum.values()) {
            List<? extends Rule> rules = this.getRules(projectName, ruleTypeEnum);
            ruleTypeEnumListMap.put(ruleTypeEnum, rules);
        }
        return ruleTypeEnumListMap;
    }

    private Map<String, Map<RuleTypeEnum, List<? extends Rule>>> getRules(Set<String> projectNames) {
        Map<String, Map<RuleTypeEnum, List<? extends Rule>>> projectName2Rules = new HashMap<>();
        for (String projectName : projectNames) {
            projectName2Rules.put(projectName, this.getRules(projectName));
        }
        return projectName2Rules;
    }

    private Map<String, Map<RuleTypeEnum, List<? extends Rule>>> getRules() {
        Set<String> projectNames = new HashSet<>(this.appManagement.getAppNames());
        return this.getRules(projectNames);
    }

    private List<? extends Rule> getRules(String projectName, RuleTypeEnum ruleTypeEnum) {
        switch (ruleTypeEnum) {
            case AUTHORITY_RULE:
                List<AuthorityRuleEntity> authorityRuleEntities = this.authorityRuleEntityLongRuleRepository.findAllByApp(projectName);
                return RuleConverter.convert2RuleList(authorityRuleEntities);
            case SYSTEM_RULE:
                List<SystemRuleEntity> systemRuleEntities = this.systemRuleEntityLongRuleRepository.findAllByApp(projectName);
                return RuleConverter.convert2RuleList(systemRuleEntities);
            case DEGRADE_RULE:
                List<DegradeRuleEntity> degradeRuleEntities = this.degradeRuleEntityLongRuleRepository.findAllByApp(projectName);
                return RuleConverter.convert2RuleList(degradeRuleEntities);
            case PARAM_FLOW_RULE:
                List<ParamFlowRuleEntity> paramFlowRuleEntities = this.paramFlowRuleEntityLongRuleRepository.findAllByApp(projectName);
                return RuleConverter.convert2RuleList(paramFlowRuleEntities);
            case FLOW_RULE:
                List<FlowRuleEntity> flowRuleEntities = this.flowRuleEntityLongRuleRepository.findAllByApp(projectName);
                return RuleConverter.convert2RuleList(flowRuleEntities);
            case GATEWAY_FLOW_RULE:
                List<GatewayFlowRuleEntity> gatewayFlowRuleEntities = this.gatewayFlowRuleEntityLongRuleRepository.findAllByApp(projectName);
                return RuleConverter.convert2RuleList(gatewayFlowRuleEntities);
            case API_DEFINITION:
                List<ApiDefinitionEntity> apiDefinitionEntities = this.apiDefinitionEntityLongRuleRepository.findAllByApp(projectName);
                return RuleConverter.convert2RuleList(apiDefinitionEntities);
            default:
                throw new IllegalArgumentException("unknown rule type " + ruleTypeEnum);
        }
    }

    public void exportAllToZip(OutputStream outputStream) throws IOException {
        Map<String, Map<RuleTypeEnum, List<? extends Rule>>> projectName2rules = this.getRules();
        writeAsZipOutputStream(outputStream, projectName2rules);
    }

    public void exportToZip(OutputStream outputStream, String projectName) throws IOException {
        Map<RuleTypeEnum, List<? extends Rule>> ruleTypeEnumListMap = this.getRules(projectName);
        writeAsZipOutputStream(outputStream, Collections.singletonMap(projectName, ruleTypeEnumListMap));
    }

    public void exportToZip(OutputStream outputStream, String projectName, RuleTypeEnum ruleTypeEnum) throws IOException {
        List<? extends Rule> rules = this.getRules(projectName, ruleTypeEnum);
        Map<String, Map<RuleTypeEnum, List<? extends Rule>>> projectName2rules = Collections.singletonMap(
                projectName,
                Collections.singletonMap(ruleTypeEnum, rules)
        );
        writeAsZipOutputStream(outputStream, projectName2rules);
    }

}
