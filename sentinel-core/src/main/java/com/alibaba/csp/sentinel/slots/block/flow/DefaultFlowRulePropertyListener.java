package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.PropertyListener;

import java.util.List;
import java.util.Map;

/**
 * @author : jiez
 * @date : 2021/7/17 10:19
 */
public class DefaultFlowRulePropertyListener extends BaseFlowRulePropertyListener {

    @Override
    public synchronized void configUpdate(List<FlowRule> value) {
        Map<String, List<FlowRule>> rules = FlowRuleUtil.buildFlowRuleMap(value);
        if (rules != null) {
            FlowRuleManager.setFlowRuleMap(rules);
        }
        RecordLog.info("[FlowRuleManager] Flow rules received: {}", rules);
    }

    @Override
    public synchronized void configLoad(List<FlowRule> conf) {
        Map<String, List<FlowRule>> rules = FlowRuleUtil.buildFlowRuleMap(conf);
        if (rules != null) {
            FlowRuleManager.setFlowRuleMap(rules);
        }
        RecordLog.info("[FlowRuleManager] Flow rules loaded: {}", rules);
    }
}
