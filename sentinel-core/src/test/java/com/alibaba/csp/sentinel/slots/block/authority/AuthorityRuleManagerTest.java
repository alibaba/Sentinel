package com.alibaba.csp.sentinel.slots.block.authority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link AuthorityRuleManager}.
 *
 * @author Eric Zhao
 */
public class AuthorityRuleManagerTest {

    public static final List<AuthorityRule> TEST_RULES_A = new ArrayList<>();
    public static final List<AuthorityRule> TEST_RULES_B = new ArrayList<>();

    @Before
    public void initRules() {
        AuthorityRule first = new AuthorityRule();
        first.setResource("/resource");
        first.setLimitApp("USER_A");
        TEST_RULES_A.add(first);

        AuthorityRule second = new AuthorityRule();
        second.setResource("/resource");
        second.setLimitApp("USER_B");
        TEST_RULES_B.add(second);
    }

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

    @Test
    public void testCurrentlyLoadRules() {
        assertEquals(0, AuthorityRuleManager.getRules().size());

        AuthorityRuleManager.loadRules(TEST_RULES_A);
        assertEquals(1, AuthorityRuleManager.getRules().size());

        final CyclicBarrier concurrent = new CyclicBarrier(2);
        final CountDownLatch quitLatch = new CountDownLatch(2);

        // Start two threads to modify rules concurrently
        for (int threadCount = 0; threadCount < 2; threadCount++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        concurrent.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < 5000; i++) {
                        AuthorityRuleManager.loadRules((i & 1) == 1 ? TEST_RULES_A : TEST_RULES_B);
                    }
                    quitLatch.countDown();
                }
            }).start();
        }

        for (int i = 0; i < 10000; i++) {
            // In a concurrent environment, the length of rules should always be 1
            assertEquals(1, AuthorityRuleManager.getRules().size());
        }

        // Wait for the two worker threads to finish executing
        try {
            quitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, AuthorityRuleManager.getRules().size());
        AuthorityRuleManager.loadRules(null);
        assertEquals(0, AuthorityRuleManager.getRules().size());
    }

    @After
    public void tearDown() {
        AuthorityRuleManager.loadRules(null);
    }
}