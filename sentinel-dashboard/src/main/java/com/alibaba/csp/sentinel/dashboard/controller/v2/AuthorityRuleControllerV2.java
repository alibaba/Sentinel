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
package com.alibaba.csp.sentinel.dashboard.controller.v2;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.ResponseCode;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author lianglin
 * @since 1.7.0
 */
@RestController
@RequestMapping(value = "v2/authority")
public class AuthorityRuleControllerV2 {

    private final Logger logger = LoggerFactory.getLogger(DegradeControllerV2.class);

    @Autowired
    @Qualifier("authorityRuleDefaultProvider")
    private DynamicRuleProvider<List<AuthorityRuleEntity>> ruleProvider;

    @Autowired
    @Qualifier("authorityRuleDefaultPublisher")
    private DynamicRulePublisher<List<AuthorityRuleEntity>> rulePublisher;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @Autowired
    private InMemoryRuleRepositoryAdapter<AuthorityRuleEntity> repository;


    @GetMapping("/rules")
    public Result<List<AuthorityRuleEntity>> apiQueryMachineRules(HttpServletRequest request, String app) {
        try {

            if(StringUtil.isBlank(app)){
                return Result.ofFail(ResponseCode.fail,"app can't be null or empty");
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);

            List<AuthorityRuleEntity> rules = ruleProvider.getRules(app);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("queryApps error:", throwable);
            return Result.ofThrowable(throwable);
        }
    }

    @PostMapping("/rule")
    public Result<AuthorityRuleEntity> apiAddRule(HttpServletRequest request, @RequestBody AuthorityRuleEntity entity) {

        try {
            Result<AuthorityRuleEntity> checkResult = checkEntity(entity);
            if (checkResult != null) {
                return checkResult;
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);

            Date date = new Date();
            entity.setGmtCreate(date).setGmtModified(date).setId(null);

            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            logger.error("add error:", throwable);
            return Result.ofThrowable(ResponseCode.fail, throwable);
        }

        return Result.ofSuccess(entity);

    }



    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<AuthorityRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }


    private <R> Result<R> checkEntity(AuthorityRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(ResponseCode.fail, "bad rule body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(ResponseCode.fail, "app can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getIp())) {
            return Result.ofFail(ResponseCode.fail, "ip can't be null or empty");
        }
        if (entity.getPort() == null || entity.getPort() <= 0) {
            return Result.ofFail(ResponseCode.fail, "port can't be null");
        }
        if (entity.getRule() == null) {
            return Result.ofFail(ResponseCode.fail, "rule can't be null");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(ResponseCode.fail, "resource name cannot be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(ResponseCode.fail, "limitApp should be valid");
        }
        if (entity.getStrategy() != RuleConstant.AUTHORITY_WHITE
                && entity.getStrategy() != RuleConstant.AUTHORITY_BLACK) {
            return Result.ofFail(ResponseCode.fail, "Unknown strategy (must be blacklist or whitelist)");
        }
        return null;
    }







}
