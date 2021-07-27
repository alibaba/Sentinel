package com.alibaba.csp.sentinel.extension.global.rule;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.extension.global.rule.flow.GlobalFlowRule;
import com.alibaba.csp.sentinel.extension.global.rule.flow.GlobalFlowRulePropertyListener;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.*;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author : jiez
 * @date : 2021/7/22 16:42
 */
public class GlobalFlowRuleTest {

    @Test
    public void testPropertyListenerLoad() {
        AssertUtil.isTrue(FlowRuleManager.getListener().getClass() == GlobalFlowRulePropertyListener.class, "flow rule property listener load fail");
    }

    @Before
    public void setUp() {
        ContextUtil.exit();
        GlobalFlowRule globalFlowRule = new GlobalFlowRule();
        globalFlowRule.setResource("[a-z]*");
        globalFlowRule.setCount(1);
        globalFlowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        FlowRuleManager.loadRules(Arrays.asList(globalFlowRule));
    }

    @Test
    public void testLoadRules() {
        GlobalFlowRule globalFlowRule = new GlobalFlowRule();
        globalFlowRule.setResource("[a-z]*");
        globalFlowRule.setCount(1);
        globalFlowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);

        FlowRule flowRule = new FlowRule();
        flowRule.setResource("test");
        flowRule.setCount(1);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

        FlowRuleManager.loadRules(Arrays.asList(globalFlowRule, flowRule));

        AssertUtil.isTrue(FlowRuleManager.getRules().size() == 1, "load normal rule fail");
        AssertUtil.isTrue(countRuleFromMap(GlobalRuleManager.getGlobalFlowRules()) == 1, "load normal rule fail");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckFlowPass() throws Exception {
        Entry entry = SphU.entry("test2");
        entry.exit();
    }

    @Test(expected = FlowException.class)
    @SuppressWarnings("unchecked")
    public void testCheckFlowBlock() throws Exception {
        for (int i = 0; i < 10; i++) {
            Entry entry = SphU.entry("test");
            entry.exit();
        }
    }

    private Integer countRuleFromMap(Map<String, List<FlowRule>> ruleMap) {
        if (Objects.isNull(ruleMap)) {
            return 0;
        }
        int count = 0;
        for (Map.Entry<String, List<FlowRule>> entry : ruleMap.entrySet()) {
            if (Objects.nonNull(entry.getValue())) {
                count = count + entry.getValue().size();
            }
        }
        return count;
    }
}
