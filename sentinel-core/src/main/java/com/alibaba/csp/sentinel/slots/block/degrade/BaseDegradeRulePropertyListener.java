package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.*;

/**
 * @author : jiez
 * @date : 2021/7/22 22:e
 */
public abstract class BaseDegradeRulePropertyListener implements PropertyListener<List<DegradeRule>> {

    /**
     * Provide to subclass for update
     *
     * @param ruleMap
     * @param circuitBreakers
     */
    protected final void updateDegradeRuleManagerRuleAndBreakerCache(Map<String, Set<DegradeRule>> ruleMap, Map<String, List<CircuitBreaker>> circuitBreakers) {
        DegradeRuleManager.updateDegradeRuleManagerRuleAndBreakerCache(ruleMap, circuitBreakers);
    }

}
