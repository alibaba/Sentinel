package com.alibaba.csp.sentinel.dashboard.controller.kie;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.rule.kie.RuleKieProvider;
import com.alibaba.csp.sentinel.dashboard.rule.kie.RuleKiePublisher;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/kie/authority")

public class KieAuthorityController {

    private final Logger logger = LoggerFactory.getLogger(KieAuthorityController.class);

    @Autowired
    @Qualifier("authorityRuleKieProvider")
    private RuleKieProvider<List<AuthorityRuleEntity>> ruleProvider;

    @Autowired
    @Qualifier("authorityRuleKiePublisher")
    private RuleKiePublisher<List<AuthorityRuleEntity>> rulePublisher;

    @GetMapping("/rules")
    public Result<List<AuthorityRuleEntity>> apiQueryRules(@RequestParam(name = "serverId") String serverId) {
        if (StringUtil.isEmpty(serverId)) {
            return Result.ofFail(-1, "Id can't be null or empty");
        }

        try {
            List<AuthorityRuleEntity> rules = ruleProvider.getRules(serverId);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying authority rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PostMapping("/{serverId}/rule")
    public Result<AuthorityRuleEntity> apiAddRule(@PathVariable("serverId") String serverId,
                                                @RequestBody AuthorityRuleEntity entity) {
        Result<AuthorityRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        try{
            rulePublisher.add(serverId, Collections.singletonList(entity));
        } catch (Throwable throwable) {
            logger.error("Error when add authority rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/{serverId}/rule")
    public Result<AuthorityRuleEntity> apiUpdateRule(@PathVariable("serverId") String serverId,
                                                   @RequestBody AuthorityRuleEntity entity) {
        Result<AuthorityRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        Date date = new Date();
        entity.setGmtModified(date);
        try {
            rulePublisher.update(serverId, Collections.singletonList(entity));
            return Result.ofSuccess(entity);
        } catch (Throwable throwable) {
            logger.error("Error when update authority rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @DeleteMapping("/{serverId}/rule/{ruleId}")
    public Result<String> delete(@PathVariable("serverId") String serverId,
                                 @PathVariable("ruleId") String ruleId) {
        if (StringUtils.isEmpty(serverId) || StringUtils.isEmpty(ruleId)) {
            return Result.ofFail(-1, "id can't be null");
        }

        try{
            rulePublisher.delete(serverId, ruleId);
            return Result.ofSuccess(ruleId);
        } catch (Throwable throwable) {
            logger.error("Error when delete rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    private <R> Result<R> checkEntityInternal(AuthorityRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(-1, "bad rule body");
        }
        if (entity.getRule() == null) {
            return Result.ofFail(-1, "rule can't be null");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource name cannot be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(-1, "limitApp should be valid");
        }
        if (entity.getStrategy() != RuleConstant.AUTHORITY_WHITE
                && entity.getStrategy() != RuleConstant.AUTHORITY_BLACK) {
            return Result.ofFail(-1, "Unknown strategy (must be blacklist or whitelist)");
        }
        return null;
    }
}
