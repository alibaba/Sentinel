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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link ParamFlowRuleManager}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowRuleManagerTest {

    @Before
    public void setUp() {
        ParamFlowRuleManager.loadRules(null);
    }

    @After
    public void tearDown() {
        ParamFlowRuleManager.loadRules(null);
    }

    @Test
    public void testLoadHotParamRulesClearingUnusedMetrics() {
        final String resA = "resA";
        ParamFlowRule ruleA = new ParamFlowRule(resA)
            .setCount(1)
            .setParamIdx(0);
        ParamFlowRuleManager.loadRules(Collections.singletonList(ruleA));
        ParamFlowSlot.getMetricsMap().put(new StringResourceWrapper(resA, EntryType.IN), new ParameterMetric());
        assertNotNull(ParamFlowSlot.getHotParamMetricForName(resA));

        final String resB = "resB";
        ParamFlowRule ruleB = new ParamFlowRule(resB)
            .setCount(2)
            .setParamIdx(1);
        ParamFlowRuleManager.loadRules(Collections.singletonList(ruleB));
        assertNull("The unused hot param metric should be cleared", ParamFlowSlot.getHotParamMetricForName(resA));
    }

    @Test
    public void testLoadHotParamRulesAndGet() {
        final String resA = "abc";
        final String resB = "foo";
        final String resC = "baz";
        // Rule A to C is for resource A.
        // Rule A is invalid.
        ParamFlowRule ruleA = new ParamFlowRule(resA).setCount(10);
        ParamFlowRule ruleB = new ParamFlowRule(resA)
            .setCount(28)
            .setParamIdx(1);
        ParamFlowRule ruleC = new ParamFlowRule(resA)
            .setCount(8)
            .setParamIdx(1)
            .setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Rule D is for resource B.
        ParamFlowRule ruleD = new ParamFlowRule(resB)
            .setCount(9)
            .setParamIdx(0)
            .setParamFlowItemList(Arrays.asList(ParamFlowItem.newItem(7L, 6), ParamFlowItem.newItem(9L, 4)));
        ParamFlowRuleManager.loadRules(Arrays.asList(ruleA, ruleB, ruleC, ruleD));

        // Test for ParamFlowRuleManager#hasRules
        assertTrue(ParamFlowRuleManager.hasRules(resA));
        assertTrue(ParamFlowRuleManager.hasRules(resB));
        assertFalse(ParamFlowRuleManager.hasRules(resC));
        // Test for ParamFlowRuleManager#getRulesOfResource
        List<ParamFlowRule> rulesForResA = ParamFlowRuleManager.getRulesOfResource(resA);
        assertEquals(2, rulesForResA.size());
        assertFalse(rulesForResA.contains(ruleA));
        assertTrue(rulesForResA.contains(ruleB));
        assertTrue(rulesForResA.contains(ruleC));
        List<ParamFlowRule> rulesForResB = ParamFlowRuleManager.getRulesOfResource(resB);
        assertEquals(1, rulesForResB.size());
        assertEquals(ruleD, rulesForResB.get(0));
        // Test for ParamFlowRuleManager#getRules
        List<ParamFlowRule> allRules = ParamFlowRuleManager.getRules();
        assertFalse(allRules.contains(ruleA));
        assertTrue(allRules.contains(ruleB));
        assertTrue(allRules.contains(ruleC));
        assertTrue(allRules.contains(ruleD));
    }

    @Test
    public void testParseHotParamExceptionItemsFailure() {
        String valueB = "Sentinel";
        Integer valueC = 6;
        char valueD = 6;
        float valueE = 11.11f;
        // Null object will not be parsed.
        ParamFlowItem itemA = new ParamFlowItem(null, 1, double.class.getName());
        // Hot item with empty class type will be treated as string.
        ParamFlowItem itemB = new ParamFlowItem(valueB, 3, null);
        ParamFlowItem itemE = new ParamFlowItem(String.valueOf(valueE), 3, "");
        // Bad count will not be parsed.
        ParamFlowItem itemC = ParamFlowItem.newItem(valueC, -5);
        ParamFlowItem itemD = new ParamFlowItem(String.valueOf(valueD), null, char.class.getName());

        List<ParamFlowItem> badItems = Arrays.asList(itemA, itemB, itemC, itemD, itemE);
        Map<Object, Integer> parsedItems = ParamFlowRuleManager.parseHotItems(badItems);

        // Value B and E will be parsed, but ignoring the type.
        assertEquals(2, parsedItems.size());
        assertEquals(itemB.getCount(), parsedItems.get(valueB));
        assertFalse(parsedItems.containsKey(valueE));
        assertEquals(itemE.getCount(), parsedItems.get(String.valueOf(valueE)));
    }

    @Test
    public void testParseHotParamExceptionItemsSuccess() {
        // Test for empty list.
        assertEquals(0, ParamFlowRuleManager.parseHotItems(null).size());
        assertEquals(0, ParamFlowRuleManager.parseHotItems(new ArrayList<ParamFlowItem>()).size());

        // Test for boxing objects and primitive types.
        Double valueA = 1.1d;
        String valueB = "Sentinel";
        Integer valueC = 6;
        char valueD = 'c';
        ParamFlowItem itemA = ParamFlowItem.newItem(valueA, 1);
        ParamFlowItem itemB = ParamFlowItem.newItem(valueB, 3);
        ParamFlowItem itemC = ParamFlowItem.newItem(valueC, 5);
        ParamFlowItem itemD = new ParamFlowItem().setObject(String.valueOf(valueD))
            .setClassType(char.class.getName())
            .setCount(7);
        List<ParamFlowItem> items = Arrays.asList(itemA, itemB, itemC, itemD);
        Map<Object, Integer> parsedItems = ParamFlowRuleManager.parseHotItems(items);
        assertEquals(itemA.getCount(), parsedItems.get(valueA));
        assertEquals(itemB.getCount(), parsedItems.get(valueB));
        assertEquals(itemC.getCount(), parsedItems.get(valueC));
        assertEquals(itemD.getCount(), parsedItems.get(valueD));
    }

    @Test
    public void testCheckValidHotParamRule() {
        // Null or empty resource;
        ParamFlowRule rule1 = new ParamFlowRule();
        ParamFlowRule rule2 = new ParamFlowRule("");
        assertFalse(ParamFlowRuleManager.isValidRule(null));
        assertFalse(ParamFlowRuleManager.isValidRule(rule1));
        assertFalse(ParamFlowRuleManager.isValidRule(rule2));

        // Invalid threshold count.
        ParamFlowRule rule3 = new ParamFlowRule("abc")
            .setCount(-1)
            .setParamIdx(1);
        assertFalse(ParamFlowRuleManager.isValidRule(rule3));

        // Parameter index not set or invalid.
        ParamFlowRule rule4 = new ParamFlowRule("abc")
            .setCount(1);
        ParamFlowRule rule5 = new ParamFlowRule("abc")
            .setCount(1)
            .setParamIdx(-1);
        assertFalse(ParamFlowRuleManager.isValidRule(rule4));
        assertFalse(ParamFlowRuleManager.isValidRule(rule5));

        ParamFlowRule goodRule = new ParamFlowRule("abc")
            .setCount(10)
            .setParamIdx(1);
        assertTrue(ParamFlowRuleManager.isValidRule(goodRule));
    }
}