package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.*;

/**
 * @author : jiez
 * @date : 2021/7/22 22:47
 */
public class DefaultDegradeRulePropertyListener extends BaseDegradeRulePropertyListener {

    @Override
    public void configUpdate(List<DegradeRule> conf) {
        reloadFrom(conf);
        RecordLog.info("[DegradeRuleManager] Degrade rules has been updated to: {}", DegradeRuleManager.getRules());
    }

    @Override
    public void configLoad(List<DegradeRule> conf) {
        reloadFrom(conf);
        RecordLog.info("[DegradeRuleManager] Degrade rules loaded: {}", DegradeRuleManager.getRules());
    }

    private synchronized void reloadFrom(List<DegradeRule> list) {
        Map<String, List<CircuitBreaker>> cbs = DegradeRuleManager.buildCircuitBreakers(list);
        Map<String, Set<DegradeRule>> rm = new HashMap<>(cbs.size());

        for (Map.Entry<String, List<CircuitBreaker>> e : cbs.entrySet()) {
            assert e.getValue() != null && !e.getValue().isEmpty();

            Set<DegradeRule> rules = new HashSet<>(e.getValue().size());
            for (CircuitBreaker cb : e.getValue()) {
                rules.add(cb.getRule());
            }
            rm.put(e.getKey(), rules);
        }

        super.updateDegradeRuleManagerRuleAndBreakerCache(rm, cbs);
    }

}
