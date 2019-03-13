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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.controller.DefaultController;

/**
 * @author jialiang.linjl
 */
@Ignore("Deprecated test for legacy FlowRule")
public class FlowRuleTest {

    @Test
    public void testFlowRule_grade() {

        FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(1);
        flowRule.setLimitApp("default");
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);

        DefaultController defaultController = new DefaultController(1, flowRule.getGrade());
        flowRule.setRater(defaultController);

        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        ClusterNode cn = mock(ClusterNode.class);

        when(context.getOrigin()).thenReturn("");
        when(node.getClusterNode()).thenReturn(cn);
        when(cn.passQps()).thenReturn(1d);

        assertFalse(flowRule.passCheck(context, node, 1));

        flowRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        defaultController = new DefaultController(1, flowRule.getGrade());
        flowRule.setRater(defaultController);
        when(cn.curThreadNum()).thenReturn(1);
        assertTrue(!flowRule.passCheck(context, node, 1));
    }

    @Test
    public void testFlowRule_strategy() {

        FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(1);
        flowRule.setLimitApp("default");
        flowRule.setStrategy(RuleConstant.STRATEGY_CHAIN);
        DefaultController defaultController = new DefaultController(1, flowRule.getGrade());
        flowRule.setRater(defaultController);
        flowRule.setRefResource("entry1");

        Context context = mock(Context.class);
        DefaultNode dn = mock(DefaultNode.class);

        when(context.getName()).thenReturn("entry1");
        when(dn.passQps()).thenReturn(1d);
        assertFalse(flowRule.passCheck(context, dn, 1));

        when(context.getName()).thenReturn("entry2");
        assertTrue(flowRule.passCheck(context, dn, 1));

        // Strategy == relate
        flowRule.setStrategy(RuleConstant.STRATEGY_CHAIN);
        ClusterNode cn = mock(ClusterNode.class);
        assertTrue(flowRule.passCheck(context, dn, 1));
    }

    @Test
    public void testOrigin() {
        FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(1);
        flowRule.setLimitApp("default");
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        DefaultController defaultController = new DefaultController(1, flowRule.getGrade());
        flowRule.setRater(defaultController);
        flowRule.setRefResource("entry1");

        Context context = mock(Context.class);
        DefaultNode dn = mock(DefaultNode.class);
        when(context.getOrigin()).thenReturn("origin1");
        when(dn.passQps()).thenReturn(1d);
        when(context.getOriginNode()).thenReturn(dn);

        /*
         * first scenario, limit app as default
         *
         */
        ClusterNode cn = mock(ClusterNode.class);
        when(dn.getClusterNode()).thenReturn(cn);
        when(cn.passQps()).thenReturn(1d);
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]) == false);
        when(cn.passQps()).thenReturn(0d);
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]));

        flowRule.setStrategy(RuleConstant.STRATEGY_CHAIN);
        flowRule.setResource("entry1");
        when(context.getName()).thenReturn("entry1");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]) == false);
        when(context.getName()).thenReturn("entry2");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]));

        // relate node
        flowRule.setStrategy(RuleConstant.STRATEGY_RELATE);
        flowRule.setResource("worong");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]));

        /*
         * second scenario test a context with the same origin1
         *
         */
        flowRule.setLimitApp("origin1");
        when(context.getName()).thenReturn("entry1");
        // direct node
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]) == false);

        // chain node
        flowRule.setResource("entry1");
        flowRule.setStrategy(RuleConstant.STRATEGY_CHAIN);
        when(context.getName()).thenReturn("entry1");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]) == false);
        when(context.getName()).thenReturn("entry2");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]));

        // relate node
        flowRule.setStrategy(RuleConstant.STRATEGY_RELATE);
        flowRule.setResource("not exits");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]));

        when(context.getOrigin()).thenReturn("origin2");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]));

        /*
         * limit app= other
         */
        flowRule.setLimitApp("other");
        flowRule.setResource("hello world");

        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]) == false);

        flowRule.setStrategy(RuleConstant.STRATEGY_CHAIN);
        flowRule.setResource("entry1");
        when(context.getName()).thenReturn("entry1");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]) == false);

        when(context.getName()).thenReturn("entry2");
        assertTrue(flowRule.passCheck(context, dn, 1, new Object[0]));
    }

}
