/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.authority;

import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

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
                .setStrategy(RuleConstant.AUTHORITY_WHITE);
            AuthorityRule ruleB = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp("appB")
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_WHITE);
            AuthorityRule ruleC = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp(origin)
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_BLACK);
            AuthorityRule ruleD = new AuthorityRule()
                .setResource(resourceName)
                .setLimitApp("appC")
                .as(AuthorityRule.class)
                .setStrategy(RuleConstant.AUTHORITY_BLACK);

            assertTrue(AuthorityRuleChecker.passCheck(ruleA, ContextUtil.getContext()));
            assertFalse(AuthorityRuleChecker.passCheck(ruleB, ContextUtil.getContext()));
            assertFalse(AuthorityRuleChecker.passCheck(ruleC, ContextUtil.getContext()));
            assertTrue(AuthorityRuleChecker.passCheck(ruleD, ContextUtil.getContext()));
        } finally {
            ContextUtil.exit();
        }
    }
}