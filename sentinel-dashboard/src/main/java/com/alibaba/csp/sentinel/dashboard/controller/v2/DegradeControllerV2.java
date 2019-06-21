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
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.ResponseCodeConstant;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.util.ParamUtils;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author lianglin
 * @since 1.7.0
 */
@RestController
@RequestMapping(value = "v2/degrade", produces = MediaType.APPLICATION_JSON_VALUE)
public class DegradeControllerV2 {

    private final Logger logger = LoggerFactory.getLogger(DegradeControllerV2.class);

    @Autowired
    @Qualifier("degradeRuleDefaultProvider")
    private DynamicRuleProvider<List<DegradeRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("degradeRuleDefaultPublisher")
    private DynamicRulePublisher<List<DegradeRuleEntity>> rulePublisher;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @Autowired
    private InMemoryRuleRepositoryAdapter<DegradeRuleEntity> repository;




    @ResponseBody
    @RequestMapping("/rules.json")
    public Result<List<DegradeRuleEntity>> queryMachineRules(HttpServletRequest request, String app, String ip, Integer port) {
        try {

            ParamUtils.checkBlank(ip, "ip can't be null or empty");
            ParamUtils.checkNull(port, "port can't be null");

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);

            List<DegradeRuleEntity> rules = ruleProvider.getRules(app);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("queryApps error:", throwable);
            return Result.ofThrowable(throwable);
        }
    }


    @ResponseBody
    @RequestMapping("/new.json")
    public Result<DegradeRuleEntity> add(HttpServletRequest request,
                                         String app, String ip, Integer port, String limitApp, String resource,
                                         Double count, Integer timeWindow, Integer grade) {


        DegradeRuleEntity entity = new DegradeRuleEntity();
        try {
            ParamUtils.checkBlank(app, "app can't be null or empty");
            ParamUtils.checkBlank(ip, "ip can't be null or empty");
            ParamUtils.checkNull(port, "port can't be null");
            ParamUtils.checkBlank(limitApp, "limitApp can't be null or empty");
            ParamUtils.checkBlank(resource, "resource can't be null or empty");
            ParamUtils.checkNull(count, "count can't be null");
            ParamUtils.checkNull(timeWindow, "timeWindow can't be null");
            ParamUtils.checkNull(grade, "grade can't be null");
            if (grade < RuleConstant.DEGRADE_GRADE_RT || grade > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
                return Result.ofFail(ResponseCodeConstant.fail, "Invalid grade: " + grade);
            }

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            authUser.authTarget(app, AuthService.PrivilegeType.WRITE_RULE);

            Date date = new Date();
            entity.setApp(app.trim())
                    .setIp(ip)
                    .setPort(port).setLimitApp(limitApp)
                    .setResource(resource)
                    .setCount(count)
                    .setTimeWindow(timeWindow)
                    .setGrade(grade)
                    .setGmtCreate(date)
                    .setGmtModified(date);
            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            logger.error("add error:", throwable);
            return Result.ofThrowable(ResponseCodeConstant.fail, throwable);
        }

        return Result.ofSuccess(entity);

    }


    @ResponseBody
    @RequestMapping("/save.json")
    public Result<DegradeRuleEntity> updateIfNotNull(HttpServletRequest request,
                                                     Long id, String app, String limitApp, String resource,
                                                     Double count, Integer timeWindow, Integer grade) {

        if (id == null) {
            return Result.ofFail(ResponseCodeConstant.fail, "id can't be null");
        }

        DegradeRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(ResponseCodeConstant.fail, "id " + id + " dose not exist");
        }

        if (grade != null) {
            if (grade < RuleConstant.DEGRADE_GRADE_RT || grade > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
                return Result.ofFail(ResponseCodeConstant.fail, "Invalid grade: " + grade);
            }
        }

        try {

            AuthService.AuthUser authUser = authService.getAuthUser(request);
            authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);

            entity.setApp(StringUtil.isNotBlank(app) ? app.trim() : entity.getApp());
            entity.setLimitApp(StringUtil.isNotBlank(limitApp) ? limitApp.trim() : entity.getLimitApp());
            entity.setResource(StringUtil.isNotBlank(resource) ? resource.trim() : entity.getResource());
            entity.setCount(count != null ? count : entity.getCount());
            entity.setTimeWindow(timeWindow != null ? timeWindow : entity.getTimeWindow());
            entity.setGrade(grade != null ? grade : entity.getGrade());
            entity.setGmtModified(new Date());

            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            logger.error("save error:", throwable);
            return Result.ofThrowable(ResponseCodeConstant.fail, throwable);
        }

        return Result.ofSuccess(entity);
    }


    @ResponseBody
    @RequestMapping("/delete.json")
    public Result<Long> delete(HttpServletRequest request, Long id) {

        if (id == null) {
            return Result.ofFail(ResponseCodeConstant.fail, "id can't be null");
        }
        DegradeRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.DELETE_RULE);

        try {
            repository.delete(id);
            publishRules(oldEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("delete error:", throwable);
            return Result.ofThrowable(ResponseCodeConstant.fail, throwable);
        }

        return Result.ofSuccess(id);
    }


    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<DegradeRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }


}
