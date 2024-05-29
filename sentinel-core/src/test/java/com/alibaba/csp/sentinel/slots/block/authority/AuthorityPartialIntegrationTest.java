package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author quguai
 * @date 2023/10/27 11:48
 */
public class AuthorityPartialIntegrationTest {

    @Test
    public void testRegex() {
        AuthorityRule authorityRule = new AuthorityRule();
        authorityRule.setRegex(true);
        authorityRule.setStrategy(1);
        authorityRule.setLimitApp("appA");
        authorityRule.setResource(".*");
        AuthorityRuleManager.loadRules(Collections.singletonList(authorityRule));
        verifyFlow("testRegex_1", "appA", false);
        verifyFlow("testRegex_2", "appA", false);
        verifyFlow("testRegex_1", "appB", true);
        verifyFlow("testRegex_2", "appB", true);
    }

    private void verifyFlow(String resource, String origin, boolean shouldPass) {
        ContextUtil.enter("a", origin);
        Entry e = null;
        try {
            e = SphU.entry(resource);
            assertTrue(shouldPass);
        } catch (BlockException e1) {
            assertFalse(shouldPass);
        } finally {
            if (e != null) {
                e.exit();
            }
            ContextUtil.exit();
        }
    }
}
