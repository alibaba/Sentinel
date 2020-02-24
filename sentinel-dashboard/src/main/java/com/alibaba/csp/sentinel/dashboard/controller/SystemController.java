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
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.domain.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author leyou(lihao)
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    private final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private RuleRepository<SystemRuleEntity, Long> repository;
    @Autowired
    private SentinelApiClient sentinelApiClient;

    private <R> Result<R> checkBasicParams(String app, String ip, Integer port) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isEmpty(ip)) {
            return Result.ofFail(-1, "ip can't be null or empty");
        }
        if (port == null) {
            return Result.ofFail(-1, "port can't be null");
        }
        if (port <= 0 || port > 65535) {
            return Result.ofFail(-1, "port should be in (0, 65535)");
        }
        return null;
    }

    @GetMapping("/rules.json")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<List<SystemRuleEntity>> apiQueryMachineRules(String app, String ip,
                                                               Integer port) {
        Result<List<SystemRuleEntity>> checkResult = checkBasicParams(app, ip, port);
        if (checkResult != null) {
            return checkResult;
        }
        try {
            List<SystemRuleEntity> rules = sentinelApiClient.fetchSystemRuleOfMachine(app, ip, port);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Query machine system rules error", throwable);
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

    @RequestMapping("/new.json")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<SystemRuleEntity> apiAdd(String app, String ip, Integer port,
                                           Double highestSystemLoad, Double highestCpuUsage, Long avgRt,
                                           Long maxThread, Double qps) {

        Result<SystemRuleEntity> checkResult = checkBasicParams(app, ip, port);
        if (checkResult != null) {
            return checkResult;
        }

        int notNullCount = countNotNullAndNotNegative(highestSystemLoad, avgRt, maxThread, qps, highestCpuUsage);
        if (notNullCount != 1) {
            return Result.ofFail(-1, "only one of [highestSystemLoad, avgRt, maxThread, qps,highestCpuUsage] "
                + "value must be set > 0, but " + notNullCount + " values get");
        }
        if (null != highestCpuUsage && highestCpuUsage > 1) {
            return Result.ofFail(-1, "highestCpuUsage must between [0.0, 1.0]");
        }
        SystemRuleEntity entity = new SystemRuleEntity();
        entity.setApp(app.trim());
        entity.setIp(ip.trim());
        entity.setPort(port);
        // -1 is a fake value
        if (null != highestSystemLoad) {
            entity.setHighestSystemLoad(highestSystemLoad);
        } else {
            entity.setHighestSystemLoad(-1D);
        }

        if (null != highestCpuUsage) {
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
            logger.error("Add SystemRule error", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(app, ip, port)) {
            logger.warn("Publish system rules fail after rule add");
        }
        return Result.ofSuccess(entity);
    }

    @GetMapping("/save.json")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<SystemRuleEntity> apiUpdateIfNotNull(Long id, String app, Double highestSystemLoad,
            Double highestCpuUsage, Long avgRt, Long maxThread, Double qps) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        SystemRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "id " + id + " dose not exist");
        }

        if (StringUtil.isNotBlank(app)) {
            entity.setApp(app.trim());
        }
        if (highestSystemLoad != null) {
            if (highestSystemLoad < 0) {
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

    @RequestMapping("/delete.json")
    @AuthAction(PrivilegeType.DELETE_RULE)
    public Result<?> delete(Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        SystemRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
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
