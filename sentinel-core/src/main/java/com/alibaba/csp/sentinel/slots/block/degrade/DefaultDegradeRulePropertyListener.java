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
public class DefaultDegradeRulePropertyListener implements DegradeRulePropertyListener {
    @Override
    public void configUpdate(List<DegradeRule> value) {

    }

    @Override
    public void configLoad(List<DegradeRule> value) {

    }

//    private synchronized void reloadFrom(List<DegradeRule> list) {
//        Map<String, List<CircuitBreaker>> cbs = buildCircuitBreakers(list);
//        Map<String, Set<DegradeRule>> rm = new HashMap<>(cbs.size());
//
//        for (Map.Entry<String, List<CircuitBreaker>> e : cbs.entrySet()) {
//            assert e.getValue() != null && !e.getValue().isEmpty();
//
//            Set<DegradeRule> rules = new HashSet<>(e.getValue().size());
//            for (CircuitBreaker cb : e.getValue()) {
//                rules.add(cb.getRule());
//            }
//            rm.put(e.getKey(), rules);
//        }
//
//        DegradeRuleManager.circuitBreakers = cbs;
//        DegradeRuleManager.ruleMap = rm;
//    }
//
//    @Override
//    public void configUpdate(List<DegradeRule> conf) {
//        reloadFrom(conf);
//        RecordLog.info("[DegradeRuleManager] Degrade rules has been updated to: {}", ruleMap);
//    }
//
//    @Override
//    public void configLoad(List<DegradeRule> conf) {
//        reloadFrom(conf);
//        RecordLog.info("[DegradeRuleManager] Degrade rules loaded: {}", ruleMap);
//    }
//
//    private Map<String, List<CircuitBreaker>> buildCircuitBreakers(List<DegradeRule> list) {
//        Map<String, List<CircuitBreaker>> cbMap = new HashMap<>(8);
//        if (list == null || list.isEmpty()) {
//            return cbMap;
//        }
//        for (DegradeRule rule : list) {
//            if (!isValidRule(rule)) {
//                RecordLog.warn("[DegradeRuleManager] Ignoring invalid rule when loading new rules: {}", rule);
//                continue;
//            }
//
//            if (StringUtil.isBlank(rule.getLimitApp())) {
//                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
//            }
//            CircuitBreaker cb = getExistingSameCbOrNew(rule);
//            if (cb == null) {
//                RecordLog.warn("[DegradeRuleManager] Unknown circuit breaking strategy, ignoring: {}", rule);
//                continue;
//            }
//
//            String resourceName = rule.getResource();
//
//            List<CircuitBreaker> cbList = cbMap.get(resourceName);
//            if (cbList == null) {
//                cbList = new ArrayList<>();
//                cbMap.put(resourceName, cbList);
//            }
//            cbList.add(cb);
//        }
//        return cbMap;
//    }
}
