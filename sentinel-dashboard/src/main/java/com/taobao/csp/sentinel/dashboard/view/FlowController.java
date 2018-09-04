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
package com.taobao.csp.sentinel.dashboard.view;

import java.util.Date;
import java.util.List;

import com.alibaba.csp.sentinel.util.StringUtil;

import com.taobao.csp.sentinel.dashboard.datasource.entity.FlowRuleEntity;
import com.taobao.csp.sentinel.dashboard.discovery.MachineInfo;
import com.taobao.csp.sentinel.dashboard.client.SentinelApiClient;
import com.taobao.csp.sentinel.dashboard.repository.rule.InMemFlowRuleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Flow rule controller.
 *
 * @author leyou
 */
@Controller
@RequestMapping(value = "/flow", produces = MediaType.APPLICATION_JSON_VALUE)
public class FlowController {
    private static Logger logger = LoggerFactory.getLogger(FlowController.class);
    @Autowired
    private InMemFlowRuleStore repository;

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @ResponseBody
    @RequestMapping("/rules.json")
    Result<List<FlowRuleEntity>> queryMachineRules(String app, String ip, Integer port) {
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
            List<FlowRuleEntity> rules = sentinelApiClient.fetchFlowRuleOfMachine(app, ip, port);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("queryApps error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @ResponseBody
    @RequestMapping("/new.json")
    Result<?> add(String app, String ip, Integer port, String limitApp, String resource, Integer grade,
                  Double count, Integer strategy, String refResource,
                  Integer controlBehavior, Integer warmUpPeriodSec, Integer maxQueueingTimeMs) {
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(ip)) {
            return Result.ofFail(-1, "ip can't be null or empty");
        }
        if (port == null) {
            return Result.ofFail(-1, "port can't be null");
        }
        if (StringUtil.isBlank(limitApp)) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (StringUtil.isBlank(resource)) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        if (grade == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (grade != 0 && grade != 1) {
            return Result.ofFail(-1, "grade must be 0 or 1, but " + grade + " got");
        }
        if (count == null) {
            return Result.ofFail(-1, "count can't be null");
        }
        if (strategy == null) {
            return Result.ofFail(-1, "strategy can't be null");
        }
        if (strategy != 0 && StringUtil.isBlank(refResource)) {
            return Result.ofFail(-1, "refResource can't be null or empty when strategy!=0");
        }
        if (controlBehavior == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        if (controlBehavior == 1 && warmUpPeriodSec == null) {
            return Result.ofFail(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (controlBehavior == 2 && maxQueueingTimeMs == null) {
            return Result.ofFail(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
        }
        FlowRuleEntity entity = new FlowRuleEntity();
        entity.setApp(app.trim());
        entity.setIp(ip.trim());
        entity.setPort(port);
        entity.setLimitApp(limitApp.trim());
        entity.setResource(resource.trim());
        entity.setGrade(grade);
        entity.setCount(count);
        entity.setStrategy(strategy);
        entity.setControlBehavior(controlBehavior);
        entity.setWarmUpPeriodSec(warmUpPeriodSec);
        entity.setMaxQueueingTimeMs(maxQueueingTimeMs);
        if (strategy != 0) {
            entity.setRefResource(refResource.trim());
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
            logger.info("publish flow rules fail after rule add");
        }
        return Result.ofSuccess(entity);
    }

    @ResponseBody
    @RequestMapping("/save.json")
    Result<?> updateIfNotNull(Long id, String app,
                              String limitApp, String resource, Integer grade,
                              Double count, Integer strategy, String refResource,
                              Integer controlBehavior, Integer warmUpPeriodSec, Integer maxQueueingTimeMs) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        FlowRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "id " + id + " dose not exist");
        }
        if (StringUtil.isNotBlank(app)) {
            entity.setApp(app.trim());
        }
        if (StringUtil.isNotBlank(limitApp)) {
            entity.setLimitApp(limitApp.trim());
        }
        if (StringUtil.isNotBlank(resource)) {
            entity.setResource(resource.trim());
        }
        if (grade != null) {
            if (grade != 0 && grade != 1) {
                return Result.ofFail(-1, "grade must be 0 or 1, but " + grade + " got");
            }
            entity.setGrade(grade);
        }
        if (count != null) {
            entity.setCount(count);
        }
        if (strategy != null) {
            if (strategy != 0 && strategy != 1 && strategy != 2) {
                return Result.ofFail(-1, "strategy must be in [0, 1, 2], but " + strategy + " got");
            }
            entity.setStrategy(strategy);
            if (strategy != 0) {
                if (StringUtil.isBlank(refResource)) {
                    return Result.ofFail(-1, "refResource can't be null or empty when strategy!=0");
                }
                entity.setRefResource(refResource.trim());
            }
        }
        if (controlBehavior != null) {
            if (controlBehavior != 0 && controlBehavior != 1 && controlBehavior != 2) {
                return Result.ofFail(-1, "controlBehavior must be in [0, 1, 2], but " + controlBehavior + " got");
            }
            if (controlBehavior == 1 && warmUpPeriodSec == null) {
                return Result.ofFail(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
            }
            if (controlBehavior == 2 && maxQueueingTimeMs == null) {
                return Result.ofFail(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
            }
            entity.setControlBehavior(controlBehavior);
            if (warmUpPeriodSec != null) {
                entity.setWarmUpPeriodSec(warmUpPeriodSec);
            }
            if (maxQueueingTimeMs != null) {
                entity.setMaxQueueingTimeMs(maxQueueingTimeMs);
            }
        }
        Date date = new Date();
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            if (entity == null) {
                return Result.ofFail(-1, "save entity fail");
            }
        } catch (Throwable throwable) {
            logger.error("save error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("publish flow rules fail after rule update");
        }
        return Result.ofSuccess(entity);
    }

    @ResponseBody
    @RequestMapping("/delete.json")
    Result<?> delete(Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        FlowRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        try {
            repository.delete(id);
        } catch (Exception e) {
            return Result.ofFail(-1, e.getMessage());
        }
        if (!publishRules(oldEntity.getApp(), oldEntity.getIp(), oldEntity.getPort())) {
            logger.info("publish flow rules fail after rule delete");
        }
        return Result.ofSuccess(id);
    }

    private boolean publishRules(String app, String ip, Integer port) {
        List<FlowRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.setFlowRuleOfMachine(app, ip, port, rules);
    }
}
