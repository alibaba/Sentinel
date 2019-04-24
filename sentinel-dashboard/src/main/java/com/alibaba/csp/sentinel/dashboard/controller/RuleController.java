package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.transpot.publish.Publisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Base rule controller
 *
 * @author longqiang
 */
public abstract class RuleController<T extends RuleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);

    @Autowired
    protected InMemoryRuleRepositoryAdapter<T> repository;

    @Autowired
    protected AuthService<HttpServletRequest> authService;

    @Autowired
    protected Publisher<T> publisher;

    @GetMapping("/rules")
    protected Result<List<T>> queryMachineRules(HttpServletRequest request, String app, String ip, Integer port) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);
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
            List<T> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
            return Result.ofSuccess(rules);
        } catch (Exception e) {
            logger.error("Error when querying rules", e);
            return Result.ofThrowable(-1, e);
        }
    }

    protected void publishRules(T ruleEntity) {
        if (!publisher.publish(ruleEntity.getApp(), ruleEntity.getIp(), ruleEntity.getPort())) {
            logger.info("publish flow rules failed after rule delete");
        }
    }

}
