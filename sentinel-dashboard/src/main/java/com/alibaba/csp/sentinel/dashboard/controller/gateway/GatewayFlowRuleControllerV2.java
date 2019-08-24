package com.alibaba.csp.sentinel.dashboard.controller.gateway;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.controller.v2.FlowControllerV2;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author dinglang
 * @since 2019-08-14 19:39
 */
@RestController
@RequestMapping(value = "/Gateway/flow/v2")
public class GatewayFlowRuleControllerV2 {
    private final Logger logger = LoggerFactory.getLogger(FlowControllerV2.class);

    @Autowired
    private InMemoryRuleRepositoryAdapter<GatewayFlowRuleEntity> repository;

    @Autowired
    @Qualifier("gatewayFlowRuleNacosProvider")
    private DynamicRuleProvider<List<GatewayFlowRuleEntity>> ruleProvider;

    @Autowired
    @Qualifier("gatewayFlowRuleNacosPublisher")
    private DynamicRulePublisher<List<GatewayFlowRuleEntity>> rulePublisher;

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @GetMapping("/rules")
    public Result<List<GatewayFlowRuleEntity>> apiQueryMachineRules(HttpServletRequest request, @RequestParam String app) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        try {
            List<GatewayFlowRuleEntity> rules = ruleProvider.getRules(app);
            if (rules != null && !rules.isEmpty()) {
                for (GatewayFlowRuleEntity entity : rules) {
                    entity.setApp(app);
                  /*  if (entity.getClusterConfig() != null && entity.getClusterConfig().getFlowId() != null) {
                        entity.setId(entity.getClusterConfig().getFlowId());
                    }*/
                }
            }
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    private <R> Result<R> checkEntityInternal(GatewayFlowRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(-1, "invalid body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
      /*  if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }*/
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
    /*    if (entity.getStrategy() == null) {
            return Result.ofFail(-1, "strategy can't be null");
        }
        if (entity.getStrategy() != 0 && StringUtil.isBlank(entity.getRefResource())) {
            return Result.ofFail(-1, "refResource can't be null or empty when strategy!=0");
        }*/
        if (entity.getControlBehavior() == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        int controlBehavior = entity.getControlBehavior();
    /*    if (controlBehavior == 1 && entity.getWarmUpPeriodSec() == null) {
            return Result.ofFail(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (controlBehavior == 2 && entity.getMaxQueueingTimeMs() == null) {
            return Result.ofFail(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
        }
        if (entity.isClusterMode() && entity.getClusterConfig() == null) {
            return Result.ofFail(-1, "cluster config should be valid");
        }*/
        return null;
    }

    @PostMapping("/rule")
    public Result<GatewayFlowRuleEntity> apiAddFlowRule(HttpServletRequest request, @RequestBody GatewayFlowRuleEntity entity) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);

        Result<GatewayFlowRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(null);
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        // entity.setLimitApp(entity.getLimitApp().trim());
        entity.setResource(entity.getResource().trim());
        try {
            entity = repository.save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            logger.error("Failed to add flow rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/rule/{id}")
    public Result<GatewayFlowRuleEntity> apiUpdateFlowRule(HttpServletRequest request,
                                                    @PathVariable("id") Long id,
                                                    @RequestBody GatewayFlowRuleEntity entity) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        GatewayFlowRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofFail(-1, "id " + id + " does not exist");
        }
        if (entity == null) {
            return Result.ofFail(-1, "invalid body");
        }
        authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.WRITE_RULE);

        entity.setApp(oldEntity.getApp());
        entity.setIp(oldEntity.getIp());
        entity.setPort(oldEntity.getPort());
        Result<GatewayFlowRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }

        entity.setId(id);
        Date date = new Date();
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            if (entity == null) {
                return Result.ofFail(-1, "save entity fail");
            }
            publishRules(oldEntity.getApp());
        } catch (Throwable throwable) {
            logger.error("Failed to update flow rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/rule/{id}")
    public Result<Long> apiDeleteRule(HttpServletRequest request, @PathVariable("id") Long id) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        GatewayFlowRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.DELETE_RULE);
        try {
            repository.delete(id);
            publishRules(oldEntity.getApp());
        } catch (Exception e) {
            return Result.ofFail(-1, e.getMessage());
        }
        return Result.ofSuccess(id);
    }

    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<GatewayFlowRuleEntity> rules = repository.findAllByApp(app);
        rulePublisher.publish(app, rules);
    }
}
