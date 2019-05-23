/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow;

import java.util.Collections;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Eric Zhao
 */
public class FlowSlotTest {

    @Before
    public void setUp() {
        ContextTestUtil.cleanUpContext();
        FlowRuleManager.loadRules(null);
    }

    @After
    public void tearDown() {
        ContextTestUtil.cleanUpContext();
        FlowRuleManager.loadRules(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckFlowPass() throws Exception {
        FlowRuleChecker checker = mock(FlowRuleChecker.class);
        FlowSlot flowSlot = new FlowSlot(checker);
        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        doCallRealMethod().when(checker).checkFlow(any(Function.class), any(ResourceWrapper.class), any(Context.class),
            any(DefaultNode.class), anyInt(), anyBoolean());

        String resA = "resAK";
        String resB = "resBK";
        FlowRule rule1 = new FlowRule(resA).setCount(10);
        FlowRule rule2 = new FlowRule(resB).setCount(10);
        // Here we only load rules for resA.
        FlowRuleManager.loadRules(Collections.singletonList(rule1));

        when(checker.canPassCheck(eq(rule1), any(Context.class), any(DefaultNode.class), anyInt(), anyBoolean()))
            .thenReturn(true);
        when(checker.canPassCheck(eq(rule2), any(Context.class), any(DefaultNode.class), anyInt(), anyBoolean()))
            .thenReturn(false);

        flowSlot.checkFlow(new StringResourceWrapper(resA, EntryType.IN), context, node, 1, false);
        flowSlot.checkFlow(new StringResourceWrapper(resB, EntryType.IN), context, node, 1, false);
    }

    @Test(expected = FlowException.class)
    @SuppressWarnings("unchecked")
    public void testCheckFlowBlock() throws Exception {
        FlowRuleChecker checker = mock(FlowRuleChecker.class);
        FlowSlot flowSlot = new FlowSlot(checker);
        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        doCallRealMethod().when(checker).checkFlow(any(Function.class), any(ResourceWrapper.class), any(Context.class),
            any(DefaultNode.class), anyInt(), anyBoolean());

        String resA = "resAK";
        FlowRule rule = new FlowRule(resA).setCount(10);
        FlowRuleManager.loadRules(Collections.singletonList(rule));

        when(checker.canPassCheck(any(FlowRule.class), any(Context.class), any(DefaultNode.class), anyInt(), anyBoolean()))
            .thenReturn(false);

        flowSlot.checkFlow(new StringResourceWrapper(resA, EntryType.IN), context, node, 1, false);
    }
}
