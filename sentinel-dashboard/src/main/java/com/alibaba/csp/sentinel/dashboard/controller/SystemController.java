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

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.AuthUser;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemSystemRuleStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author leyou(lihao)
 */
@Controller
@RequestMapping(value = "/system", produces = MediaType.APPLICATION_JSON_VALUE)
public class SystemController {
    private static Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private InMemSystemRuleStore repository;
    @Autowired
    private SentinelApiClient sentinelApiClient;
    @Autowired
    private AuthService<HttpServletRequest> authService;

    @ResponseBody
    @RequestMapping("/rules.json")
    Result<List<SystemRuleEntity>> queryMachineRules(HttpServletRequest request, String app, String ip, Integer port) {
        AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(app, PrivilegeType.READ_RULE);
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isEmpty(ip)) {
            return Result.ofFail(-1, "ip can't be null or empty");
        }
        if (port == null) {
            return Result.ofFail(-1, "port can't be null");
        }
        try {
            List<SystemRuleEntity> rules = sentinelApiClient.fetchSystemRuleOfMachine(app, ip, port);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("queryApps error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    private int countNotNullAndNotNegative(Number... values) {
        int notNullCount = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && values[i].doubleValue() >= 0) {
                notNullCount++;
            }
        }
        return notNullCount;
    }

    /**
     * @modify
     * 目前代码里做了如下几点修改：
     * 1】修改属性名称avgLoad 修改为highestSystemLoad
     * 2】修改属性名称avgCpu 修改为highestCpuUsage
     * 3】调用countNotNullAndNotNegative非空校验方法里增加参数highestCpuUsage,
     * 4】修改notNullCount部分判断错误提示语,因为目前countNotNullAndNotNegative里针对=0的情况也做了限制
     * 5】对于highestSystemLoad、highestCpuUsage增加>1 的这个判断
     * @author tianyang5@yeah.net
     * @time 2019年7月17日 18:30:32
     * @modify
     *
     * @param request
     * @param app
     * @param ip
     * @param port
     * @param highestSystemLoad
     * @param highestCpuUsage
     * @param avgRt
     * @param maxThread
     * @param qps
     * @return
     */
    @ResponseBody
    @RequestMapping("/new.json")
    Result<?> add(HttpServletRequest request,
                  String app, String ip, Integer port, Double highestSystemLoad,Double highestCpuUsage, Long avgRt, Long maxThread, Double qps) {
        AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(app, PrivilegeType.WRITE_RULE);
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(ip)) {
            return Result.ofFail(-1, "ip can't be null or empty");
        }
        if (port == null) {
            return Result.ofFail(-1, "port can't be null");
        }

        int notNullCount = countNotNullAndNotNegative(highestSystemLoad, avgRt, maxThread, qps,highestCpuUsage);
        if (notNullCount != 1) {
            return Result.ofFail(-1, "only one of [highestSystemLoad, avgRt, maxThread, qps,highestCpuUsage] "
                + "value must be set > 0, but " + notNullCount + " values get");
        }
        if (  null!=highestCpuUsage && 1 < highestCpuUsage ) {
            return Result.ofFail(-1, "highestCpuUsage must <= 1");
        }
        SystemRuleEntity entity = new SystemRuleEntity();
        entity.setApp(app.trim());
        entity.setIp(ip.trim());
        entity.setPort(port);
        // -1 is a fake value
        if ( null != highestSystemLoad  ) {
            entity.setHighestSystemLoad(highestSystemLoad);
        } else {
            entity.setHighestSystemLoad(-1D);
        }

        if ( null !=  highestCpuUsage ) {
            entity.setHighestCpuUsage(highestCpuUsage);
        } else {
            entity.setHighestCpuUsage(-1D);
        }

        if (avgRt != null) {
            entity.setAvgRt(avgRt);
        } else {
            entity.setAvgRt(-1L);
        }
        if (maxThread != null) {
            entity.setMaxThread(maxThread);
        } else {
            entity.setMaxThread(-1L);
        }
        if (qps != null) {
            entity.setQps(qps);
        } else {
            entity.setQps(-1D);
        }
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
        } catch (Throwable throwable) {
            logger.error("add error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(app, ip, port)) {
            logger.info("publish system rules fail after rule add");
        }
        return Result.ofSuccess(entity);
    }

    /**
     * @modify
     * 目前代码里做了如下几点修改：
     * 1】修改属性名称avgLoad 修改为highestSystemLoad
     * 2】修改属性名称avgCpu 修改为highestCpuUsage
     * 1】对于highestSystemLoad、highestCpuUsage增加>1 的这个判断,调整原先<0的判断,调整为<=0  等于0的这种规则设置了也没有意义
     * @author tianyang5@yeah.net
     * @time 2019年7月17日 18:30:32
     * @modify
     *
     * @param request
     * @param id
     * @param app
     * @param highestSystemLoad
     * @param highestCpuUsage
     * @param avgRt
     * @param maxThread
     * @param qps
     * @return
     */
    @ResponseBody
    @RequestMapping("/save.json")
    Result<?> updateIfNotNull(HttpServletRequest request,
                              Long id, String app, Double highestSystemLoad,Double highestCpuUsage, Long avgRt, Long maxThread, Double qps) {
        AuthUser authUser = authService.getAuthUser(request);
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        SystemRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "id " + id + " dose not exist");
        }
        authUser.authTarget(entity.getApp(), PrivilegeType.WRITE_RULE);
        if (StringUtil.isNotBlank(app)) {
            entity.setApp(app.trim());
        }
        if (highestSystemLoad != null) {
            if (highestSystemLoad <= 0) {
                return Result.ofFail(-1, "highestSystemLoad must >= 0");
            }
            entity.setHighestSystemLoad(highestSystemLoad);
        }
        if (highestCpuUsage != null) {
            if (highestCpuUsage < 0) {
                return Result.ofFail(-1, "highestCpuUsage must >= 0");
            }
            if (highestCpuUsage > 1) {
                return Result.ofFail(-1, "highestCpuUsage must <= 1");
            }
            entity.setHighestCpuUsage(highestCpuUsage);
        }
        if (avgRt != null) {
            if (avgRt < 0) {
                return Result.ofFail(-1, "avgRt must >= 0");
            }
            entity.setAvgRt(avgRt);
        }
        if (maxThread != null) {
            if (maxThread < 0) {
                return Result.ofFail(-1, "maxThread must >= 0");
            }
            entity.setMaxThread(maxThread);
        }
        if (qps != null) {
            if (qps < 0) {
                return Result.ofFail(-1, "qps must >= 0");
            }
            entity.setQps(qps);
        }
        Date date = new Date();
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
        } catch (Throwable throwable) {
            logger.error("save error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("publish system rules fail after rule update");
        }
        return Result.ofSuccess(entity);
    }

    @ResponseBody
    @RequestMapping("/delete.json")
    Result<?> delete(HttpServletRequest request, Long id) {
        AuthUser authUser = authService.getAuthUser(request);
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        SystemRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        authUser.authTarget(oldEntity.getApp(), PrivilegeType.DELETE_RULE);
        try {
            repository.delete(id);
        } catch (Throwable throwable) {
            logger.error("delete error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(oldEntity.getApp(), oldEntity.getIp(), oldEntity.getPort())) {
            logger.info("publish system rules fail after rule delete");
        }
        return Result.ofSuccess(id);
    }

    private boolean publishRules(String app, String ip, Integer port) {
        List<SystemRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.setSystemRuleOfMachine(app, ip, port, rules);
    }
}
