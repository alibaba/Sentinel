package com.alibaba.csp.sentinel.slots.block.flow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class FlowRuleComparatorTest {

    @Test
    public void testFlowRuleComparator() {
        FlowRule ruleA = new FlowRule("abc")
            .setCount(10);
        ruleA.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
        FlowRule ruleB = new FlowRule("abc");
        ruleB.setLimitApp("originA");
        FlowRule ruleC = new FlowRule("abc");
        ruleC.setLimitApp("originB");
        FlowRule ruleD = new FlowRule("abc");
        ruleD.setLimitApp(RuleConstant.LIMIT_APP_OTHER);
        FlowRule ruleE = new FlowRule("abc")
            .setCount(20);
        ruleE.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);

        List<FlowRule> list = Arrays.asList(ruleA, ruleB, ruleC, ruleD, ruleE);
        FlowRuleComparator comparator = new FlowRuleComparator();
        Collections.sort(list, comparator);
        List<FlowRule> expected = Arrays.asList(ruleB, ruleC, ruleD, ruleA, ruleE);
        assertOrderEqual(expected.size(), expected, list);
    }

    private void assertOrderEqual(int size, List<FlowRule> expected, List<FlowRule> actual) {
        for (int i = 0; i < size; i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testNullVsNonNullLimitApp() {
        FlowRuleComparator comparator = new FlowRuleComparator();

        // o1 has null limitApp, o2 has non-null limitApp
        FlowRule ruleNull = new FlowRule("abc");
        ruleNull.setLimitApp(null);
        FlowRule ruleNonNull = new FlowRule("abc");
        ruleNonNull.setLimitApp("originA");

        // Comparator should NOT return 0 when one is null and the other is not
        int result = comparator.compare(ruleNull, ruleNonNull);
        assertNotEquals("null vs non-null should not be equal", 0, result);

        // Symmetry: compare(o2, o1) should be -compare(o1, o2)
        int reverseResult = comparator.compare(ruleNonNull, ruleNull);
        assertEquals("comparator must be antisymmetric", -result, reverseResult);
    }

    @Test
    public void testDifferentNonDefaultLimitApps() {
        FlowRuleComparator comparator = new FlowRuleComparator();

        // Two rules with different non-DEFAULT limitApp values
        FlowRule ruleA = new FlowRule("abc");
        ruleA.setLimitApp("originA");
        FlowRule ruleB = new FlowRule("abc");
        ruleB.setLimitApp("originB");

        // Different apps should NOT compare as equal (0)
        int result = comparator.compare(ruleA, ruleB);
        assertNotEquals("different non-default apps should not be equal", 0, result);

        // Symmetry check
        int reverseResult = comparator.compare(ruleB, ruleA);
        assertEquals("comparator must be antisymmetric", -result, reverseResult);
    }
}