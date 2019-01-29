/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class ParamFlowRuleUtilTest {
    @Test
    public void testCheckValidHotParamRule() {
        // Null or empty resource;
        ParamFlowRule rule1 = new ParamFlowRule();
        ParamFlowRule rule2 = new ParamFlowRule("");
        assertFalse(ParamFlowRuleUtil.isValidRule(null));
        assertFalse(ParamFlowRuleUtil.isValidRule(rule1));
        assertFalse(ParamFlowRuleUtil.isValidRule(rule2));

        // Invalid threshold count.
        ParamFlowRule rule3 = new ParamFlowRule("abc")
            .setCount(-1)
            .setParamIdx(1);
        assertFalse(ParamFlowRuleUtil.isValidRule(rule3));

        // Parameter index not set or invalid.
        ParamFlowRule rule4 = new ParamFlowRule("abc")
            .setCount(1);
        ParamFlowRule rule5 = new ParamFlowRule("abc")
            .setCount(1)
            .setParamIdx(-1);
        assertFalse(ParamFlowRuleUtil.isValidRule(rule4));
        assertFalse(ParamFlowRuleUtil.isValidRule(rule5));

        ParamFlowRule goodRule = new ParamFlowRule("abc")
            .setCount(10)
            .setParamIdx(1);
        assertTrue(ParamFlowRuleUtil.isValidRule(goodRule));
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
        Map<Object, Integer> parsedItems = ParamFlowRuleUtil.parseHotItems(badItems);

        // Value B and E will be parsed, but ignoring the type.
        assertEquals(2, parsedItems.size());
        assertEquals(itemB.getCount(), parsedItems.get(valueB));
        assertFalse(parsedItems.containsKey(valueE));
        assertEquals(itemE.getCount(), parsedItems.get(String.valueOf(valueE)));
    }

    @Test
    public void testParseHotParamExceptionItemsSuccess() {
        // Test for empty list.
        assertEquals(0, ParamFlowRuleUtil.parseHotItems(null).size());
        assertEquals(0, ParamFlowRuleUtil.parseHotItems(new ArrayList<ParamFlowItem>()).size());

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
        Map<Object, Integer> parsedItems = ParamFlowRuleUtil.parseHotItems(items);
        assertEquals(itemA.getCount(), parsedItems.get(valueA));
        assertEquals(itemB.getCount(), parsedItems.get(valueB));
        assertEquals(itemC.getCount(), parsedItems.get(valueC));
        assertEquals(itemD.getCount(), parsedItems.get(valueD));
    }
}