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

import java.util.Arrays;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Eric Zhao
 */
public class FlowRuleCheckerTest {

    @Test
    public void testDefaultLimitAppFlowSelectNode() {
        DefaultNode node = mock(DefaultNode.class);
        ClusterNode cn = mock(ClusterNode.class);
        when(node.getClusterNode()).thenReturn(cn);
        Context context = mock(Context.class);

        // limitApp: default
        FlowRule rule = new FlowRule("testDefaultLimitAppFlowSelectNode").setCount(1);
        assertEquals(cn, FlowRuleChecker.selectNodeByRequesterAndStrategy(rule, context, node));
    }

    @Test
    public void testCustomOriginFlowSelectNode() {
        String origin = "appA";
        String limitAppB = "appB";

        DefaultNode node = mock(DefaultNode.class);
        DefaultNode originNode = mock(DefaultNode.class);
        ClusterNode cn = mock(ClusterNode.class);
        when(node.getClusterNode()).thenReturn(cn);
        Context context = mock(Context.class);
        when(context.getOrigin()).thenReturn(origin);
        when(context.getOriginNode()).thenReturn(originNode);

        FlowRule rule = new FlowRule("testCustomOriginFlowSelectNode").setCount(1);
        rule.setLimitApp(origin);
        // Origin matches, return the origin node.
        assertEquals(originNode, FlowRuleChecker.selectNodeByRequesterAndStrategy(rule, context, node));

        rule.setLimitApp(limitAppB);
        // Origin mismatch, no node found.
        assertNull(FlowRuleChecker.selectNodeByRequesterAndStrategy(rule, context, node));
    }

    @Test
    public void testOtherOriginFlowSelectNode() {
        String originA = "appA";
        String originB = "appB";

        DefaultNode node = mock(DefaultNode.class);
        DefaultNode originNode = mock(DefaultNode.class);
        ClusterNode cn = mock(ClusterNode.class);
        when(node.getClusterNode()).thenReturn(cn);
        Context context = mock(Context.class);
        when(context.getOriginNode()).thenReturn(originNode);

        FlowRule ruleA = new FlowRule("testOtherOriginFlowSelectNode").setCount(1);
        ruleA.setLimitApp(originA);
        FlowRule ruleB = new FlowRule("testOtherOriginFlowSelectNode").setCount(2);
        ruleB.setLimitApp(RuleConstant.LIMIT_APP_OTHER);
        FlowRuleManager.loadRules(Arrays.asList(ruleA, ruleB));

        // Origin matches other, return the origin node.
        when(context.getOrigin()).thenReturn(originB);
        assertEquals(originNode, FlowRuleChecker.selectNodeByRequesterAndStrategy(ruleB, context, node));

        // Origin matches limitApp of an existing rule, so no nodes are selected.
        when(context.getOrigin()).thenReturn(originA);
        assertNull(FlowRuleChecker.selectNodeByRequesterAndStrategy(ruleB, context, node));
    }

    @Test
    public void testSelectNodeForEmptyReference() {
        DefaultNode node = mock(DefaultNode.class);
        Context context = mock(Context.class);

        FlowRule rule = new FlowRule("testSelectNodeForEmptyReference")
            .setCount(1)
            .setStrategy(RuleConstant.STRATEGY_CHAIN);
        assertNull(FlowRuleChecker.selectReferenceNode(rule, context, node));
    }

    @Test
    public void testSelectNodeForRelateReference() {
        String refResource = "testSelectNodeForRelateReference_refResource";

        DefaultNode node = mock(DefaultNode.class);
        ClusterNode refCn = mock(ClusterNode.class);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(refResource, EntryType.IN), refCn);
        Context context = mock(Context.class);

        FlowRule rule = new FlowRule("testSelectNodeForRelateReference")
            .setCount(1)
            .setStrategy(RuleConstant.STRATEGY_RELATE)
            .setRefResource(refResource);
        assertEquals(refCn, FlowRuleChecker.selectReferenceNode(rule, context, node));
    }

    @Test
    public void testSelectReferenceNodeForContextEntrance() {
        String contextName = "good_context";

        DefaultNode node = mock(DefaultNode.class);
        Context context = mock(Context.class);

        FlowRule rule = new FlowRule("testSelectReferenceNodeForContextEntrance")
            .setCount(1)
            .setStrategy(RuleConstant.STRATEGY_CHAIN)
            .setRefResource(contextName);

        when(context.getName()).thenReturn(contextName);
        assertEquals(node, FlowRuleChecker.selectReferenceNode(rule, context, node));

        when(context.getName()).thenReturn("other_context");
        assertNull(FlowRuleChecker.selectReferenceNode(rule, context, node));
    }

    @Test
    public void testPassCheckNullLimitApp() {
        FlowRule rule = new FlowRule("abc").setCount(1);
        rule.setLimitApp(null);
        FlowRuleChecker checker = new FlowRuleChecker();
        assertTrue(checker.canPassCheck(rule, null, null, 1));
    }

    @Test
    public void testPassCheckSelectEmptyNodeSuccess() {
        FlowRule rule = new FlowRule("abc").setCount(1);
        rule.setLimitApp("abc");

        DefaultNode node = mock(DefaultNode.class);
        Context context = mock(Context.class);
        when(context.getOrigin()).thenReturn("def");

        FlowRuleChecker checker = new FlowRuleChecker();
        assertTrue(checker.canPassCheck(rule, context, node, 1));
    }

    @Before
    public void setUp() throws Exception {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }

    @After
    public void tearDown() throws Exception {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }
}