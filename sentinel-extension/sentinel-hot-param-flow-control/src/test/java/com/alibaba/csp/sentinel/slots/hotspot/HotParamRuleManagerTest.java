package com.alibaba.csp.sentinel.slots.hotspot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link HotParamRuleManager}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotParamRuleManagerTest {

    @Before
    public void setUp() {
        HotParamRuleManager.loadRules(null);
    }

    @After
    public void tearDown() {
        HotParamRuleManager.loadRules(null);
    }

    @Test
    public void testLoadHotParamRulesAndGet() {
        final String resA = "abc";
        final String resB = "foo";
        final String resC = "baz";
        // Rule A to C is for resource A.
        // Rule A is invalid.
        HotParamRule ruleA = new HotParamRule(resA).setCount(10);
        HotParamRule ruleB = new HotParamRule(resA)
            .setCount(28)
            .setParamIdx(1);
        HotParamRule ruleC = new HotParamRule(resA)
            .setCount(8)
            .setParamIdx(1)
            .setBlockGrade(RuleConstant.FLOW_GRADE_THREAD);
        // Rule D is for resource B.
        HotParamRule ruleD = new HotParamRule(resB)
            .setCount(9)
            .setParamIdx(0)
            .setHotItemList(Arrays.asList(HotItem.newHotItem(7L, 6), HotItem.newHotItem(9L, 4)));
        HotParamRuleManager.loadRules(Arrays.asList(ruleA, ruleB, ruleC, ruleD));

        // Test for HotParamRuleManager#hasRules
        assertTrue(HotParamRuleManager.hasRules(resA));
        assertTrue(HotParamRuleManager.hasRules(resB));
        assertFalse(HotParamRuleManager.hasRules(resC));
        // Test for HotParamRuleManager#getRulesOfResource
        List<HotParamRule> rulesForResA = HotParamRuleManager.getRulesOfResource(resA);
        assertEquals(2, rulesForResA.size());
        assertFalse(rulesForResA.contains(ruleA));
        assertTrue(rulesForResA.contains(ruleB));
        assertTrue(rulesForResA.contains(ruleC));
        List<HotParamRule> rulesForResB = HotParamRuleManager.getRulesOfResource(resB);
        assertEquals(1, rulesForResB.size());
        assertEquals(ruleD, rulesForResB.get(0));
        // Test for HotParamRuleManager#getRules
        List<HotParamRule> allRules = HotParamRuleManager.getRules();
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
        HotItem itemA = new HotItem(null, 1, double.class.getName());
        // Hot item with empty class type will be treated as string.
        HotItem itemB = new HotItem(valueB, 3, null);
        HotItem itemE = new HotItem(String.valueOf(valueE), 3, "");
        // Bad count will not be parsed.
        HotItem itemC = HotItem.newHotItem(valueC, -5);
        HotItem itemD = new HotItem(String.valueOf(valueD), null, char.class.getName());

        List<HotItem> badItems = Arrays.asList(itemA, itemB, itemC, itemD, itemE);
        Map<Object, Integer> parsedItems = HotParamRuleManager.parseHotItems(badItems);

        // Value B and E will be parsed, but ignoring the type.
        assertEquals(2, parsedItems.size());
        assertEquals(itemB.getCount(), parsedItems.get(valueB));
        assertFalse(parsedItems.containsKey(valueE));
        assertEquals(itemE.getCount(), parsedItems.get(String.valueOf(valueE)));
    }

    @Test
    public void testParseHotParamExceptionItemsSuccess() {
        // Test for empty list.
        assertEquals(0, HotParamRuleManager.parseHotItems(null).size());
        assertEquals(0, HotParamRuleManager.parseHotItems(new ArrayList<HotItem>()).size());

        // Test for boxing objects and primitive types.
        Double valueA = 1.1d;
        String valueB = "Sentinel";
        Integer valueC = 6;
        char valueD = 'c';
        HotItem itemA = HotItem.newHotItem(valueA, 1);
        HotItem itemB = HotItem.newHotItem(valueB, 3);
        HotItem itemC = HotItem.newHotItem(valueC, 5);
        HotItem itemD = new HotItem().setObject(String.valueOf(valueD))
            .setClassType(char.class.getName())
            .setCount(7);
        List<HotItem> items = Arrays.asList(itemA, itemB, itemC, itemD);
        Map<Object, Integer> parsedItems = HotParamRuleManager.parseHotItems(items);
        assertEquals(itemA.getCount(), parsedItems.get(valueA));
        assertEquals(itemB.getCount(), parsedItems.get(valueB));
        assertEquals(itemC.getCount(), parsedItems.get(valueC));
        assertEquals(itemD.getCount(), parsedItems.get(valueD));
    }

    @Test
    public void testCheckValidHotParamRule() {
        // Null or empty resource;
        HotParamRule rule1 = new HotParamRule();
        HotParamRule rule2 = new HotParamRule("");
        assertFalse(HotParamRuleManager.isValidRule(null));
        assertFalse(HotParamRuleManager.isValidRule(rule1));
        assertFalse(HotParamRuleManager.isValidRule(rule2));

        // Invalid threshold count.
        HotParamRule rule3 = new HotParamRule("abc")
            .setCount(-1)
            .setParamIdx(1);
        assertFalse(HotParamRuleManager.isValidRule(rule3));

        // Parameter index not set or invalid.
        HotParamRule rule4 = new HotParamRule("abc")
            .setCount(1);
        HotParamRule rule5 = new HotParamRule("abc")
            .setCount(1)
            .setParamIdx(-1);
        assertFalse(HotParamRuleManager.isValidRule(rule4));
        assertFalse(HotParamRuleManager.isValidRule(rule5));

        HotParamRule goodRule = new HotParamRule("abc")
            .setCount(10)
            .setParamIdx(1);
        assertTrue(HotParamRuleManager.isValidRule(goodRule));
    }
}