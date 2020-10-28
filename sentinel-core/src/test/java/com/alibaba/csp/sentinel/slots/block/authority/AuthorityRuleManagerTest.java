package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link AuthorityRuleManager}.
 *
 * @author Eric Zhao
 * @author Weihua
 */
public class AuthorityRuleManagerTest {

    public static final List<AuthorityRule> STATIC_RULES_1 = new ArrayList<>();
    public static final List<AuthorityRule> STATIC_RULES_2 = new ArrayList<>();

    static {
        AuthorityRule first = new AuthorityRule();
        first.setResource("/a/b/c");
        first.setLimitApp("postman");
        STATIC_RULES_1.add(first);

        AuthorityRule second = new AuthorityRule();
        second.setResource("/a/b/c");
        second.setLimitApp("jmeter");
        STATIC_RULES_2.add(second);
    }

    @After
    public void setUp() {
        AuthorityRuleManager.loadRules(null);
    }

    @Test
    public void testLoadAndGetRules() throws InterruptedException {
        AuthorityRuleManager.loadRules(STATIC_RULES_1);
        assertEquals(1, AuthorityRuleManager.getRules().size()); // the initial size
        final CountDownLatch latchStart = new CountDownLatch(1);
        final CountDownLatch latchEnd = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latchStart.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    return;
                }
                for(int i = 0; i < 10000; i++){
                    //to guarantee that they're different and change happens
                    AuthorityRuleManager.loadRules(i % 2 == 0 ? STATIC_RULES_2 : STATIC_RULES_1);
                }
                latchEnd.countDown();
            }
        }).start();

        latchStart.countDown();
        for(int i = 0; i < 10000; i++){
            //The initial size is 1, and the size after updating should also be 1,
            //if the actual size is 0, that must be called after clear(),
            // but before putAll() in RulePropertyListener.configUpdate
            assertEquals(1, AuthorityRuleManager.getRules().size());
        }
        latchEnd.await(10, TimeUnit.SECONDS);
        AuthorityRuleManager.loadRules(null);
        assertEquals(0, AuthorityRuleManager.getAuthorityRules().size());
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