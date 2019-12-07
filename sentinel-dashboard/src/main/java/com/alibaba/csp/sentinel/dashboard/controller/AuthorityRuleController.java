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
package com.alibaba.csp.sentinel.dashboard.controller;

import java.util.Date;
import java.util.List;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Eric Zhao
 * @since 0.2.1
 */
@RestController
@RequestMapping(value = "/authority")
public class AuthorityRuleController {

    private final Logger logger = LoggerFactory.getLogger(AuthorityRuleController.class);

    @Autowired
    private SentinelApiClient sentinelApiClient;
    @Autowired
    private RuleRepository<AuthorityRuleEntity, Long> repository;

    @GetMapping("/rules")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<List<AuthorityRuleEntity>> apiQueryAllRulesForMachine(@RequestParam String app,
                                                                        @RequestParam String ip,
                                                                        @RequestParam Integer port) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app cannot be null or empty");
        }
        if (StringUtil.isEmpty(ip)) {
            return Result.ofFail(-1, "ip cannot be null or empty");
        }
        if (port == null || port <= 0) {
            return Result.ofFail(-1, "Invalid parameter: port");
        }
        try {
            List<AuthorityRuleEntity> rules = sentinelApiClient.fetchAuthorityRulesOfMachine(app, ip, port);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying authority rules", throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }

    private <R> Result<R> checkEntityInternal(AuthorityRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(-1, "bad rule body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getIp())) {
            return Result.ofFail(-1, "ip can't be null or empty");
        }
        if (entity.getPort() == null || entity.getPort() <= 0) {
            return Result.ofFail(-1, "port can't be null");
        }
        if (entity.getRule() == null) {
            return Result.ofFail(-1, "rule can't be null");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource name cannot be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(-1, "limitApp should be valid");
        }
        if (entity.getStrategy() != RuleConstant.AUTHORITY_WHITE
            && entity.getStrategy() != RuleConstant.AUTHORITY_BLACK) {
            return Result.ofFail(-1, "Unknown strategy (must be blacklist or whitelist)");
        }
        return null;
    }

    @PostMapping("/rule")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<AuthorityRuleEntity> apiAddAuthorityRule(@RequestBody AuthorityRuleEntity entity) {
        Result<AuthorityRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(null);
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
        } catch (Throwable throwable) {
            logger.error("Failed to add authority rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("Publish authority rules failed after rule add");
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/rule/{id}")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<AuthorityRuleEntity> apiUpdateParamFlowRule(@PathVariable("id") Long id,
                                                              @RequestBody AuthorityRuleEntity entity) {
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        Result<AuthorityRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(id);
        Date date = new Date();
        entity.setGmtCreate(null);
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            if (entity == null) {
                return Result.ofFail(-1, "Failed to save authority rule");
            }
        } catch (Throwable throwable) {
            logger.error("Failed to save authority rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("Publish authority rules failed after rule update");
        }
        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/rule/{id}")
    @AuthAction(PrivilegeType.DELETE_RULE)
    public Result<Long> apiDeleteRule(@PathVariable("id") Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id cannot be null");
        }
        AuthorityRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        try {
            repository.delete(id);
        } catch (Exception e) {
            return Result.ofFail(-1, e.getMessage());
        }
        if (!publishRules(oldEntity.getApp(), oldEntity.getIp(), oldEntity.getPort())) {
            logger.error("Publish authority rules failed after rule delete");
        }
        return Result.ofSuccess(id);
    }

    private boolean publishRules(String app, String ip, Integer port) {
        List<AuthorityRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.setAuthorityRuleOfMachine(app, ip, port, rules);
    }
}
