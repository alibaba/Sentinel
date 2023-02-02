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

package com.alibaba.csp.sentinel.dashboard.rule.aop;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleStoreFactory;
import com.alibaba.csp.sentinel.dashboard.rule.RuleType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author FengJianxin
 * @since 1.8.4
 */
@Aspect
public class SentinelApiClientAspect {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelApiClientAspect.class);

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("sentinel-dashboard-api-aspect"));

    @Resource
    private DynamicRuleStoreFactory factory;


    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchGatewayFlowRules(..))")
    public void fetchGatewayFlowRulesPointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.modifyGatewayFlowRules(..))")
    public void modifyGatewayFlowRulesPointcut() {
    }

    /**
     * 拉取网关流控规则配置
     */
    @Around("fetchGatewayFlowRulesPointcut()")
    public Object fetchGatewayFlowRules(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRulesWithCompletableFuture(pjp, RuleType.GW_FLOW);
    }

    /**
     * 推送网关流控规则配置
     */
    @Around("modifyGatewayFlowRulesPointcut()")
    public Object modifyGatewayFlowRules(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.GW_FLOW);
    }


    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchApis(..))")
    public void fetchApisPointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.modifyApis(..))")
    public void modifyApisPointcut() {
    }

    /**
     * 拉取 api 分组规则配置
     */
    @Around("fetchApisPointcut()")
    public Object fetchApis(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRulesWithCompletableFuture(pjp, RuleType.GW_API_GROUP);
    }

    /**
     * 推送 api 分组规则配置
     */
    @Around("modifyApisPointcut()")
    public Object modifyApis(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.GW_API_GROUP);
    }


    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchFlowRuleOfMachine(..))")
    public void fetchFlowRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setFlowRuleOfMachineAsync(..))")
    public void setFlowRuleOfMachineAsyncPointcut() {
    }

    /**
     * 拉取流控规则配置
     */
    @Around("fetchFlowRuleOfMachinePointcut()")
    public Object fetchFlowRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.FLOW);
    }

    /**
     * 推送流控规则配置
     */
    @SuppressWarnings("unchecked")
    @Around("setFlowRuleOfMachineAsyncPointcut()")
    public Object setFlowRuleOfMachineAsync(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRulesWithCompletableFuture(pjp, RuleType.FLOW);
    }


    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchDegradeRuleOfMachine(..))")
    public void fetchDegradeRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setDegradeRuleOfMachine(..))")
    public void setDegradeRuleOfMachinePointcut() {
    }

    /**
     * 拉取熔断规则配置
     */
    @Around("fetchDegradeRuleOfMachinePointcut()")
    public Object fetchDegradeRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.DEGRADE);
    }

    /**
     * 推送熔断规则配置
     */
    @Around("setDegradeRuleOfMachinePointcut()")
    public Object setDegradeRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.DEGRADE);
    }


    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchParamFlowRulesOfMachine(..))")
    public void fetchParamFlowRulesOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setParamFlowRuleOfMachine(..))")
    public void setParamFlowRuleOfMachinePointcut() {
    }

    /**
     * 拉取热点规则配置
     */
    @Around("fetchParamFlowRulesOfMachinePointcut()")
    public Object fetchParamFlowRulesOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRulesWithCompletableFuture(pjp, RuleType.PARAM_FLOW);
    }

    /**
     * 推送热点规则配置
     */
    @Around("setParamFlowRuleOfMachinePointcut()")
    public Object setParamFlowRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRulesWithCompletableFuture(pjp, RuleType.PARAM_FLOW);
    }


    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchSystemRuleOfMachine(..))")
    public void fetchSystemRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setSystemRuleOfMachine(..))")
    public void setSystemRuleOfMachinePointcut() {
    }

    /**
     * 拉取系统规则配置
     */
    @Around("fetchSystemRuleOfMachinePointcut()")
    public Object fetchSystemRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.SYSTEM);
    }

    /**
     * 推送系统规则配置
     */
    @Around("setSystemRuleOfMachinePointcut()")
    public Object setSystemRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.SYSTEM);
    }


    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchAuthorityRulesOfMachine(..))")
    public void fetchAuthorityRulesOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setAuthorityRuleOfMachine(..))")
    public void setAuthorityRuleOfMachinePointcut() {
    }

    /**
     * 拉取授权规则规则配置
     */
    @Around("fetchAuthorityRulesOfMachinePointcut()")
    public Object fetchAuthorityRulesOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.AUTHORITY);
    }

    /**
     * 推送授权规则配置
     */
    @Around("setAuthorityRuleOfMachinePointcut()")
    public Object setAuthorityRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.AUTHORITY);
    }

    private Object fetchRules(ProceedingJoinPoint pjp, RuleType ruleType) throws Throwable {
        DynamicRuleStore<?> dynamicRuleStore = factory.getDynamicRuleStoreByType(ruleType);
        if (dynamicRuleStore == null) {
            return pjp.proceed();
        }
        Object[] args = pjp.getArgs();
        String app = (String) args[0];
        return dynamicRuleStore.getRules(app);
    }

    private CompletableFuture<Object> fetchRulesWithCompletableFuture(ProceedingJoinPoint pjp, RuleType ruleType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchRules(pjp, ruleType);
            } catch (Throwable e) {
                throw new RuntimeException("fetch rules error: " + ruleType.getName(), e);
            }
        }, EXECUTOR);
    }


    @SuppressWarnings("unchecked")
    private boolean publishRules(ProceedingJoinPoint pjp, RuleType ruleType) {
        DynamicRuleStore<RuleEntity> dynamicRuleStore = factory.getDynamicRuleStoreByType(ruleType);
        Object[] args = pjp.getArgs();
        String app = (String) args[0];
        List<RuleEntity> rules = (List<RuleEntity>) args[3];
        try {
            dynamicRuleStore.publish(app, rules);
            return true;
        } catch (Exception e) {
            LOG.error("publish rules error", e);
            return false;
        }
    }

    private CompletableFuture<Void> publishRulesWithCompletableFuture(ProceedingJoinPoint pjp, RuleType ruleType) {
        return CompletableFuture.runAsync(() -> publishRules(pjp, ruleType), EXECUTOR);
    }


}
