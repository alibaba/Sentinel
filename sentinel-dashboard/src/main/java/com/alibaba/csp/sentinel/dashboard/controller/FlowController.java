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

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.vo.req.MachineReqVo;
import com.alibaba.csp.sentinel.dashboard.vo.req.rule.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.vo.req.rule.flow.UpdateFlowRuleReqVo;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Flow rule controller (v2).
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
@RestController
@RequestMapping(value = "/flow")
public class FlowController {

    private final Logger logger = LoggerFactory.getLogger(FlowController.class);

    @Autowired
    private InMemoryRuleRepositoryAdapter<FlowRuleEntity> repository;

    @Autowired
//    @Qualifier("flowRuleDefaultProvider")
//    private DynamicRuleProvider<FlowRuleEntity> ruleProvider;
    private DynamicRuleProvider<FlowRuleEntity> ruleProvider;

    @Autowired
//    @Qualifier("flowRuleDefaultPublisher")
    private DynamicRulePublisher<FlowRuleEntity> rulePublisher;

//    @Autowired
//    private DynamicRuleProvider<DegradeRuleEntity> degradeRuleProvider;

    @GetMapping("/rules")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<List<FlowRuleEntity>> queryFlowRuleList(MachineReqVo reqVo) {
        String app = reqVo.getApp();
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        String ip = reqVo.getIp();
        Integer port = reqVo.getPort();
        boolean operateApp = StringUtil.isEmpty(ip) || port == null;

        try {
            List<FlowRuleEntity> rules;
            if (operateApp) {
                rules = ruleProvider.getRules(app);
            } else {
                rules = ruleProvider.getRules(app, ip, port);
            }

            if (rules != null && !rules.isEmpty()) {
                for (FlowRuleEntity entity : rules) {
                    if (operateApp) {
                        entity.setIp(null);
                        entity.setPort(null);
                    } else {
                        entity.setIp(ip);
                        entity.setPort(port);
                    }
                    entity.setApp(app);
                    if (entity.getClusterConfig() != null && entity.getClusterConfig().getFlowId() != null) {
                        entity.setId(entity.getClusterConfig().getFlowId());
                    }
                }
            }
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    private <R> Result<R> checkEntityInternal(FlowRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(-1, "invalid body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        if (entity.getGrade() == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (entity.getGrade() != 0 && entity.getGrade() != 1) {
            return Result.ofFail(-1, "grade must be 0 or 1, but " + entity.getGrade() + " got");
        }
        if (entity.getCount() == null || entity.getCount() < 0) {
            return Result.ofFail(-1, "count should be at lease zero");
        }
        if (entity.getStrategy() == null) {
            return Result.ofFail(-1, "strategy can't be null");
        }
        if (entity.getStrategy() != 0 && StringUtil.isBlank(entity.getRefResource())) {
            return Result.ofFail(-1, "refResource can't be null or empty when strategy!=0");
        }
        if (entity.getControlBehavior() == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        int controlBehavior = entity.getControlBehavior();
        if (controlBehavior == 1 && entity.getWarmUpPeriodSec() == null) {
            return Result.ofFail(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (controlBehavior == 2 && entity.getMaxQueueingTimeMs() == null) {
            return Result.ofFail(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
        }
        if (entity.isClusterMode() && entity.getClusterConfig() == null) {
            return Result.ofFail(-1, "cluster config should be valid");
        }
        return null;
    }

    @PostMapping("/rule")
    @AuthAction(value = PrivilegeType.WRITE_RULE)
    public Result<FlowRuleEntity> addFlowRule(@RequestBody AddFlowRuleReqVo reqVo) {
        if (reqVo == null) {
            return Result.ofFail(-1, "invalid body");
        }

        if (StringUtil.isBlank(reqVo.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(reqVo.getResource())) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        if (StringUtil.isBlank(reqVo.getLimitApp())) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (reqVo.getGrade() == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (reqVo.getGrade() != 0 && reqVo.getGrade() != 1) {
            return Result.ofFail(-1, "grade must be 0 or 1, but " + reqVo.getGrade() + " got");
        }
        if (reqVo.getCount() == null || reqVo.getCount() < 0) {
            return Result.ofFail(-1, "count should be at lease zero");
        }
        if (reqVo.getStrategy() == null) {
            return Result.ofFail(-1, "strategy can't be null");
        }
        if (reqVo.getStrategy() != 0 && StringUtil.isBlank(reqVo.getRefResource())) {
            return Result.ofFail(-1, "refResource can't be null or empty when strategy!=0");
        }
        if (reqVo.getControlBehavior() == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        int controlBehavior = reqVo.getControlBehavior();
        if (controlBehavior == 1 && reqVo.getWarmUpPeriodSec() == null) {
            return Result.ofFail(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (controlBehavior == 2 && reqVo.getMaxQueueingTimeMs() == null) {
            return Result.ofFail(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
        }
        if (reqVo.getClusterMode() && reqVo.getClusterConfig() == null) {
            return Result.ofFail(-1, "cluster config should be valid");
        }

        FlowRuleEntity entity = new FlowRuleEntity();
        Date date = new Date();
        entity.setApp(reqVo.getApp());
        entity.setIp(reqVo.getIp());
        entity.setPort(reqVo.getPort());
        entity.setResource(reqVo.getResource());
        entity.setLimitApp(reqVo.getLimitApp());
        entity.setGrade(reqVo.getGrade());
        entity.setCount(reqVo.getCount());
        entity.setStrategy(reqVo.getStrategy());
        entity.setControlBehavior(reqVo.getControlBehavior());
        entity.setRefResource(reqVo.getRefResource());
        entity.setWarmUpPeriodSec(reqVo.getWarmUpPeriodSec());
        entity.setMaxQueueingTimeMs(reqVo.getMaxQueueingTimeMs());
        entity.setClusterMode(reqVo.getClusterMode());
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
        if (clusterConfigReqVo != null) {
            ClusterFlowConfig clusterFlowConfig = new ClusterFlowConfig();
            clusterFlowConfig.setThresholdType(clusterConfigReqVo.getThresholdType());
            clusterFlowConfig.setFallbackToLocalWhenFail(clusterConfigReqVo.getFallbackToLocalWhenFail());
        }
        entity.setGmtCreate(date);
        entity.setGmtModified(date);


//        Result<FlowRuleEntity> checkResult = checkEntityInternal(entity);
//        if (checkResult != null) {
//            return checkResult;
//        }
//        entity.setId(null);
//        Date date = new Date();
//        entity.setGmtCreate(date);
//        entity.setGmtModified(date);
//        entity.setLimitApp(entity.getLimitApp().trim());
//        entity.setResource(entity.getResource().trim());

        try {
            entity = repository.save(entity);
//            publishRules(entity.getApp());
            publishRules(reqVo);
        } catch (Throwable throwable) {
            logger.error("Failed to add flow rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/rule/{id}")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<FlowRuleEntity> apiUpdateFlowRule(@PathVariable("id") Long id, @RequestBody UpdateFlowRuleReqVo reqVo) {
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        FlowRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "id " + id + " does not exist");
        }
        if (StringUtil.isBlank(reqVo.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(reqVo.getLimitApp())) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (reqVo.getGrade() == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (reqVo.getGrade() != 0 && reqVo.getGrade() != 1) {
            return Result.ofFail(-1, "grade must be 0 or 1, but " + reqVo.getGrade() + " got");
        }
        if (reqVo.getCount() == null || reqVo.getCount() < 0) {
            return Result.ofFail(-1, "count should be at lease zero");
        }
        if (reqVo.getStrategy() == null) {
            return Result.ofFail(-1, "strategy can't be null");
        }
        if (reqVo.getStrategy() != 0 && StringUtil.isBlank(reqVo.getRefResource())) {
            return Result.ofFail(-1, "refResource can't be null or empty when strategy!=0");
        }
        if (reqVo.getControlBehavior() == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        int controlBehavior = reqVo.getControlBehavior();
        if (controlBehavior == 1 && reqVo.getWarmUpPeriodSec() == null) {
            return Result.ofFail(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (controlBehavior == 2 && reqVo.getMaxQueueingTimeMs() == null) {
            return Result.ofFail(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
        }
        if (reqVo.getClusterMode() && reqVo.getClusterConfig() == null) {
            return Result.ofFail(-1, "cluster config should be valid");
        }

        Date date = new Date();
        entity.setLimitApp(reqVo.getLimitApp());
        entity.setGrade(reqVo.getGrade());
        entity.setCount(reqVo.getCount());
        entity.setStrategy(reqVo.getStrategy());
        entity.setControlBehavior(reqVo.getControlBehavior());
        entity.setRefResource(reqVo.getRefResource());
        entity.setWarmUpPeriodSec(reqVo.getWarmUpPeriodSec());
        entity.setMaxQueueingTimeMs(reqVo.getMaxQueueingTimeMs());
        entity.setClusterMode(reqVo.getClusterMode());
        AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
        if (clusterConfigReqVo != null) {
            ClusterFlowConfig clusterFlowConfig = new ClusterFlowConfig();
            clusterFlowConfig.setThresholdType(clusterConfigReqVo.getThresholdType());
            clusterFlowConfig.setFallbackToLocalWhenFail(clusterConfigReqVo.getFallbackToLocalWhenFail());
        }
        entity.setGmtModified(date);

//        if (entity == null) {
//            return Result.ofFail(-1, "invalid body");
//        }
//
//        entity.setApp(oldEntity.getApp());
//        entity.setIp(oldEntity.getIp());
//        entity.setPort(oldEntity.getPort());
//        Result<FlowRuleEntity> checkResult = checkEntityInternal(entity);
//        if (checkResult != null) {
//            return checkResult;
//        }
//
//        entity.setId(id);
//        Date date = new Date();
//        entity.setGmtCreate(oldEntity.getGmtCreate());
//        entity.setGmtModified(date);

        try {
            entity = repository.save(entity);
            if (entity == null) {
                return Result.ofFail(-1, "save entity fail");
            }
//            publishRules(oldEntity.getApp());
            publishRules(reqVo);
        } catch (Throwable throwable) {
            logger.error("Failed to update flow rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/rule/{id}")
//    @PostMapping("/rule/delete/{id}")
    @AuthAction(PrivilegeType.DELETE_RULE)
    public Result<Long> apiDeleteRule(@PathVariable("id") Long id, @RequestBody MachineReqVo reqVo) {
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        FlowRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }

        try {
            repository.delete(id);
//            publishRules(oldEntity.getApp());
            publishRules(reqVo);
        } catch (Exception e) {
            return Result.ofFail(-1, e.getMessage());
        }
        return Result.ofSuccess(id);
    }

    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<FlowRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }

    private void publishRules(/*@NonNull*/ MachineReqVo reqVo) throws Exception {
        boolean operateApp = isOperateApp(reqVo);
        if (operateApp) {
            List<FlowRuleEntity> rules = repository.findAllByApp(reqVo.getApp());
            rulePublisher.publish(reqVo.getApp(), rules);
        } else {
            List<FlowRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(reqVo.getApp(), reqVo.getIp(), reqVo.getPort()));
            rulePublisher.publish(reqVo.getApp(), reqVo.getIp(), reqVo.getPort(), rules);
        }
    }

    protected static boolean isOperateApp(MachineReqVo reqVo) {
        String ip = reqVo.getIp();
        Integer port = reqVo.getPort();
        return StringUtil.isEmpty(ip) || port == null;
    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        DynamicRuleProvider bean = applicationContext.getBean(DynamicRuleProvider.class);
//        System.out.println(bean);
//    }
}
