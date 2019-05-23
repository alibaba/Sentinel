package com.alibaba.csp.sentinel.slots.block.authority;

import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link AuthorityRuleManager}.
 *
 * @author Eric Zhao
 */
public class AuthorityRuleManagerTest {

    @After
    public void setUp() {
        AuthorityRuleManager.loadRules(null);
    }

    @Test
    public void testLoadRules() {
        String resourceName = "testLoadRules";

        AuthorityRule rule = new AuthorityRule();
        rule.setResource(resourceName);
        rule.setLimitApp("a,b");
        rule.setStrategy(RuleConstant.AUTHORITY_WHITE);
        AuthorityRuleManager.loadRules(Collections.singletonList(rule));

        List<AuthorityRule> rules = AuthorityRuleManager.getRules();
        assertEquals(1, rules.size());
        assertEquals(rule, rules.get(0));

        AuthorityRuleManager.loadRules(Collections.singletonList(new AuthorityRule()));
        rules = AuthorityRuleManager.getRules();
        assertEquals(0, rules.size());
    }

    @Test
    public void testIsValidRule() {
        AuthorityRule ruleA = new AuthorityRule();
        AuthorityRule ruleB = null;
        AuthorityRule ruleC = new AuthorityRule();
        ruleC.setResource("abc");
        AuthorityRule ruleD = new AuthorityRule();
        ruleD.setResource("bcd").setLimitApp("abc");

        assertFalse(AuthorityRuleManager.isValidRule(ruleA));
        assertFalse(AuthorityRuleManager.isValidRule(ruleB));
        assertFalse(AuthorityRuleManager.isValidRule(ruleC));
        assertTrue(AuthorityRuleManager.isValidRule(ruleD));
    }

    @After
    public void tearDown() {
        AuthorityRuleManager.loadRules(null);
    }
}