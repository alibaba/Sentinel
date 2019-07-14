package com.alibaba.csp.sentinel.dashboard.controller.v2;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public abstract class BaseControllerV2<T extends RuleEntity> {


    @Autowired
    private AuthService<HttpServletRequest> authService;

    protected abstract Logger getLogger();

    protected abstract InMemoryRuleRepositoryAdapter<T> getRepository();

    protected abstract DynamicRuleProvider<List<T>> getRuleProvider();

    protected abstract DynamicRulePublisher<List<T>> getRulePublisher();

    protected abstract void preSave(T entity);

    protected abstract void preUpdate(T entity, T oldEntity);

    protected abstract <R> Result<R> checkEntityInternal(T entity);

    @GetMapping("/rules")
    public Result<List<T>> apiQueryMachineRules(HttpServletRequest request, @RequestParam String app) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        try {
            List<T> rules = getRuleProvider().getRules(app);
            rules = getRepository().saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            getLogger().error("Error when querying  rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PostMapping("/rule")
    public Result<T> apiAddFlowRule(HttpServletRequest request, @RequestBody T entity) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);

        Result<T> checkResult = checkEntity(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(null);
        preSave(entity);
        try {
            entity = getRepository().save(entity);
            publishRules(entity.getApp());
        } catch (Throwable throwable) {
            getLogger().error("Failed to add  rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/rule/{id}")
    public Result<T> apiUpdateFlowRule(HttpServletRequest request,
                                       @PathVariable("id") Long id,
                                       @RequestBody T entity) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        T oldEntity = getRepository().findById(id);
        if (oldEntity == null) {
            return Result.ofFail(-1, "id " + id + " does not exist");
        }
        if (entity == null) {
            return Result.ofFail(-1, "invalid body");
        }
        authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.WRITE_RULE);

        preUpdate(entity, oldEntity);
        Result<T> checkResult = checkEntity(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(id);
        try {
            entity = getRepository().save(entity);
            if (entity == null) {
                return Result.ofFail(-1, "save entity fail");
            }
            publishRules(oldEntity.getApp());
        } catch (Throwable throwable) {
            getLogger().error("Failed to update rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    private  Result<T> checkEntity(T entity){
        if (entity == null) {
            return Result.ofFail(-1, "invalid body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        return checkEntityInternal(entity);
    }

    @DeleteMapping("/rule/{id}")
    public Result<Long> apiDeleteRule(HttpServletRequest request, @PathVariable("id") Long id) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        T oldEntity = getRepository().findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.DELETE_RULE);
        try {
            getRepository().delete(id);
            publishRules(oldEntity.getApp());
        } catch (Exception e) {
            return Result.ofFail(-1, e.getMessage());
        }
        return Result.ofSuccess(id);
    }



    private void publishRules(/*@NonNull*/ String app) throws Exception {
        List<T> rules = getRepository().findAllByApp(app);
        getRulePublisher().publish(app, rules);
    }
}
