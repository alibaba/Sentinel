package com.alibaba.csp.sentinel.extension.rule;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : jiez
 * @date : 2021/7/21 17:23
 */
public class GlobalRuleManager {

    private static volatile Map<String, List<FlowRule>> flowRules = new HashMap<>();




}
