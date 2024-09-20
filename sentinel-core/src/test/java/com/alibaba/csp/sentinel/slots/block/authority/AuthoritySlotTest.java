/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Test cases for {@link AuthoritySlot}.
 *
 * @author Eric Zhao
 * @author cdfive
 */
public class AuthoritySlotTest {

    private AuthoritySlot authoritySlot = new AuthoritySlot();

    @Test
    public void testFireEntry() throws Throwable {
        AuthoritySlot slot = mock(AuthoritySlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        DefaultNode node = mock(DefaultNode.class);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        slot.entry(context, resourceWrapper, node, 1, false);

        verify(slot).entry(context, resourceWrapper, node, 1, false);
        verify(slot).checkBlackWhiteAuthority(resourceWrapper, context);
        // Verify fireEntry method has been called, and only once
        verify(slot).fireEntry(context, resourceWrapper, node, 1, false);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testFireExit() throws Throwable {
        AuthoritySlot slot = mock(AuthoritySlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);

        doCallRealMethod().when(slot).exit(context, resourceWrapper, 1);
        slot.exit(context, resourceWrapper, 1);

        verify(slot).exit(context, resourceWrapper, 1);
        // Verify fireExit method has been called, and only once
        verify(slot).fireExit(context, resourceWrapper, 1);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testEntry() throws Throwable {
        AuthoritySlot slot = mock(AuthoritySlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        DefaultNode node = mock(DefaultNode.class);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        slot.entry(context, resourceWrapper, node, 1, false);

        // Verify checkBlackWhiteAuthority firstly, then fireEntry, and both are called, and only once
        InOrder inOrder = inOrder(slot);
        inOrder.verify(slot).checkBlackWhiteAuthority(resourceWrapper, context);
        inOrder.verify(slot).fireEntry(context, resourceWrapper, node, 1, false);
        inOrder.verifyNoMoreInteractions();
    }

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