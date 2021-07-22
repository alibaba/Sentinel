package com.alibaba.csp.sentinel.extension.rule.flow;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.extension.rule.GlobalFlowRule;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleSelector;
import com.alibaba.csp.sentinel.slots.block.RuleSelectorLoader;
import com.alibaba.csp.sentinel.slots.block.flow.*;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

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
        globalFlowRule.setResource("*");
        globalFlowRule.setCount(1);
        FlowRuleManager.loadRules(null);
    }

    @After
    public void tearDown() {
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckFlowPass() throws Exception {
        SphU.entry("test");
    }

    @Test(expected = FlowException.class)
    @SuppressWarnings("unchecked")
    public void testCheckFlowBlock() throws Exception {
    }
}
