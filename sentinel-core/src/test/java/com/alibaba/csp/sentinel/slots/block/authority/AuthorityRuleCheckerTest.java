package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.enums.AuthorityStrategy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link AuthorityRuleChecker}.
 *
 * @author Eric Zhao
 */
public class AuthorityRuleCheckerTest {

    @Before
    public void setUp() {
        ContextTestUtil.cleanUpContext();
    }

    @Test
    public void testPassCheck() {
        String origin = "appA";
        ContextUtil.enter("entrance", origin);
        try {
            String resourceName = "testPassCheck";
            AuthorityRule ruleA = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp(origin + ",appB")
                .as(AuthorityRule.class)
                .setStrategy(AuthorityStrategy.White);
            AuthorityRule ruleB = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp("appB")
                .as(AuthorityRule.class)
                .setStrategy(AuthorityStrategy.White);
            AuthorityRule ruleC = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp(origin)
                .as(AuthorityRule.class)
                .setStrategy(AuthorityStrategy.Black);
            AuthorityRule ruleD = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp("appC")
                .as(AuthorityRule.class)
                .setStrategy(AuthorityStrategy.Black);

            assertTrue(AuthorityRuleChecker.passCheck(ruleA, ContextUtil.getContext()));
            assertFalse(AuthorityRuleChecker.passCheck(ruleB, ContextUtil.getContext()));
            assertFalse(AuthorityRuleChecker.passCheck(ruleC, ContextUtil.getContext()));
            assertTrue(AuthorityRuleChecker.passCheck(ruleD, ContextUtil.getContext()));
        } finally {
            ContextUtil.exit();
        }
    }
}