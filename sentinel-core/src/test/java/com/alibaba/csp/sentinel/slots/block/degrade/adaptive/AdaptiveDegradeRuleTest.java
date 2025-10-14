package com.alibaba.csp.sentinel.slots.block.degrade.adaptive;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AdaptiveDegradeRule}.
 *
 * @author ylnxwlp
 */
public class AdaptiveDegradeRuleTest {

    private static final String RESOURCE_NAME = "testService";
    private AdaptiveDegradeRule rule;

    @Before
    public void setUp() {
        rule = new AdaptiveDegradeRule(RESOURCE_NAME);
    }

    @Test
    public void testConstructorAndResourceName() {
        AdaptiveDegradeRule newRule = new AdaptiveDegradeRule(RESOURCE_NAME);
        assertEquals(RESOURCE_NAME, newRule.getResource());
    }

    @Test
    public void testEnabledDefaultValue() {
        assertFalse("Default enabled value should be true", rule.isEnabled());
    }

    @Test
    public void testSetterAndGetter() {
        rule.setEnabled(false);
        assertFalse("isEnabled should return false after setEnabled(false)", rule.isEnabled());

        rule.setEnabled(true);
        assertTrue("isEnabled should return true after setEnabled(true)", rule.isEnabled());
    }

    @Test
    public void testEquals_Reflexive() {
        assertEquals("Rule should be equal to itself", rule, rule);
    }

    @Test
    public void testEquals_Symmetric() {
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_NAME);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule(RESOURCE_NAME);

        assertEquals("Rules with same resource and enabled should be equal", rule1, rule2);
        assertEquals("Equality should be symmetric", rule2, rule1);
    }

    @Test
    public void testEquals_Transitive() {
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_NAME);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule(RESOURCE_NAME);
        AdaptiveDegradeRule rule3 = new AdaptiveDegradeRule(RESOURCE_NAME);

        assertEquals("rule1 should equal rule2", rule1, rule2);
        assertEquals("rule2 should equal rule3", rule2, rule3);
        assertEquals("rule1 should equal rule3 by transitivity", rule1, rule3);
    }

    @Test
    public void testEquals_WithNull() {
        assertNotEquals("Rule should not equal null", null, rule);
    }

    @Test
    public void testEquals_WithDifferentType() {
        Object differentType = new Object();
        assertNotEquals("Rule should not equal different type", rule, differentType);

        AbstractRule differentRuleType = new AbstractRule() {
        };
        differentRuleType.setResource(RESOURCE_NAME);
        assertNotEquals("Rule should not equal different AbstractRule subclass", rule, differentRuleType);
    }

    @Test
    public void testEquals_WithDifferentResource() {
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_NAME);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule("differentResource");

        assertNotEquals("Rules with different resources should not be equal", rule1, rule2);
        assertNotEquals("Rules with different resources should not be equal", rule2, rule1);
    }

    @Test
    public void testEquals_WithDifferentEnabled() {
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_NAME);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule(RESOURCE_NAME);
        rule1.setEnabled(true);
        rule2.setEnabled(false);
        assertNotEquals("Rules with different enabled values should not be equal", rule1, rule2);
        assertNotEquals("Rules with different enabled values should not be equal", rule2, rule1);
    }

    @Test
    public void testEquals_WithSameConfiguration() {
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_NAME);
        rule1.setEnabled(false);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule(RESOURCE_NAME);
        rule2.setEnabled(false);

        assertEquals("Rules with same resource and enabled should be equal", rule1, rule2);
        assertEquals("Rules with same resource and enabled should be equal", rule2, rule1);
    }

    @Test
    public void testHashCode_Consistency() {
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_NAME);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule(RESOURCE_NAME);

        assertEquals("Equal rules should have same hashCode", rule1.hashCode(), rule2.hashCode());
    }

    @Test
    public void testHashCode_DifferentConfigurations() {
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_NAME);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule("differentResource");
        AdaptiveDegradeRule rule3 = new AdaptiveDegradeRule(RESOURCE_NAME);
        rule3.setEnabled(true);

        assertNotEquals("Rules with different resources should ideally have different hashCodes",
                rule1.hashCode(), rule2.hashCode());
        assertNotEquals("Rules with different enabled values should ideally have different hashCodes",
                rule1.hashCode(), rule3.hashCode());
    }

    @Test
    public void testHashCode_SameConfiguration() {
        AdaptiveDegradeRule rule1 = new AdaptiveDegradeRule(RESOURCE_NAME);
        rule1.setEnabled(false);
        AdaptiveDegradeRule rule2 = new AdaptiveDegradeRule(RESOURCE_NAME);
        rule2.setEnabled(false);

        assertEquals("Rules with same configuration should have same hashCode", rule1.hashCode(), rule2.hashCode());
    }
}