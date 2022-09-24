package com.alibaba.csp.sentinel.slots.block.authority;

import java.util.Collections;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link AuthoritySlot}.
 *
 * @author Eric Zhao
 */
public class AuthoritySlotTest {

    private AuthoritySlot authoritySlot = new AuthoritySlot();

    @Test
    public void testCheckAuthorityNoExceptionItemsSuccess() throws Exception {
        String origin = "appA";
        String resourceName = "testCheckAuthorityNoExceptionItemsSuccess";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        ContextUtil.enter("entrance", origin);
        try {
            AuthorityRule ruleA = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp(origin + ",appB")
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_WHITE);

            AuthorityRuleManager.loadRules(Collections.singletonList(ruleA));
            authoritySlot.checkBlackWhiteAuthority(resourceWrapper, ContextUtil.getContext());

            AuthorityRule ruleB = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp("appD")
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_BLACK);

            AuthorityRuleManager.loadRules(Collections.singletonList(ruleB));
            authoritySlot.checkBlackWhiteAuthority(resourceWrapper, ContextUtil.getContext());
        } finally {
            ContextUtil.exit();
        }
    }

    @Test(expected = AuthorityException.class)
    public void testCheckAuthorityNoExceptionItemsBlackFail() throws Exception {
        String origin = "appA";
        String resourceName = "testCheckAuthorityNoExceptionItemsBlackFail";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        ContextUtil.enter("entrance", origin);
        try {
            AuthorityRule ruleA = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp(origin + ",appC")
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_BLACK);

            AuthorityRuleManager.loadRules(Collections.singletonList(ruleA));
            authoritySlot.checkBlackWhiteAuthority(resourceWrapper, ContextUtil.getContext());
        } finally {
            ContextUtil.exit();
        }
    }

    @Test(expected = AuthorityException.class)
    public void testCheckAuthorityNoExceptionItemsWhiteFail() throws Exception {
        String origin = "appA";
        String resourceName = "testCheckAuthorityNoExceptionItemsWhiteFail";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        ContextUtil.enter("entrance", origin);
        try {
            AuthorityRule ruleB = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp("appB, appE")
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_WHITE);

            AuthorityRuleManager.loadRules(Collections.singletonList(ruleB));
            authoritySlot.checkBlackWhiteAuthority(resourceWrapper, ContextUtil.getContext());
        } finally {
            ContextUtil.exit();
        }
    }

    /*@Test
    public void testCheckAuthorityWithExceptionItemsSuccess() throws Exception {
        String origin = "ipA";
        String resourceName = "interfaceA";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        ContextUtil.enter("entrance", origin);
        try {
            AuthorityRule ruleEx = new AuthorityRule()
                .setResource("methodXXX")
                .setLimitApp(origin)
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_WHITE);

            AuthorityRule ruleA = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp("appA,appB")
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_BLACK)
                .addExceptionItem(ruleEx);

            AuthorityRuleManager.loadRules(Collections.singletonList(ruleA));
            authoritySlot.checkBlackWhiteAuthority(resourceWrapper, ContextUtil.getContext());
        } finally {
            ContextUtil.exit();
        }
    }*/

    @Before
    public void setUp() {
        ContextTestUtil.cleanUpContext();
        AuthorityRuleManager.loadRules(null);
    }

    @After
    public void tearDown() {
        ContextTestUtil.cleanUpContext();
        AuthorityRuleManager.loadRules(null);
    }
}