package com.alibaba.csp.sentinel.extension.global.rule;

import com.alibaba.csp.sentinel.extension.global.rule.flow.GlobalFlowRuleSelector;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleSelectorLoader;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.junit.Test;

/**
 * @author : jiez
 * @date : 2021/7/22 16:12
 */
public class RuleSelectorTest {

    @Test
    public void testEntryExitCounts() {
        AssertUtil.isTrue(RuleSelectorLoader.getSelector(RuleConstant.RULE_SELECTOR_TYPE_FLOW_RULE).getClass() == GlobalFlowRuleSelector.class, "flow rule selector load fail");
    }
}
