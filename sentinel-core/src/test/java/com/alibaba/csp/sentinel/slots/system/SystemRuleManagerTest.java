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
package com.alibaba.csp.sentinel.slots.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class SystemRuleManagerTest {

    @Test
    public void testLoadInvalidRules() {
        SystemRule rule1 = new SystemRule();
        rule1.setHighestSystemLoad(-0.9d);
        SystemRule rule2 = new SystemRule();
        rule2.setHighestCpuUsage(2.7d);
        SystemRuleManager.loadRules(Arrays.asList(rule1, rule2));
        assertEquals(0, SystemRuleManager.getRules().size());
    }

    @Test
    public void testLoadAndGetRules() {
        SystemRule rule1 = new SystemRule();
        rule1.setHighestSystemLoad(1.2d);
        SystemRule rule2 = new SystemRule();
        rule2.setMaxThread(17);
        SystemRule rule3 = new SystemRule();
        rule3.setHighestCpuUsage(0.7d);
        SystemRule rule4 = new SystemRule();
        rule4.setQps(1500);
        SystemRule rule5 = new SystemRule();
        rule5.setAvgRt(50);
        SystemRuleManager.loadRules(Arrays.asList(rule1, rule2, rule3, rule4, rule5));
        assertEquals(1.2d, SystemRuleManager.getSystemLoadThreshold(), 0.01);
        assertEquals(17, SystemRuleManager.getMaxThreadThreshold());
        assertEquals(0.7d, SystemRuleManager.getCpuUsageThreshold(), 0.01);
        assertEquals(1500, SystemRuleManager.getInboundQpsThreshold(), 0.01);
        assertEquals(50, SystemRuleManager.getRtThreshold());
    }

    @Test
    public void testLoadDuplicateTypeOfRules() {
        SystemRule rule1 = new SystemRule();
        rule1.setHighestSystemLoad(1.2d);
        SystemRule rule2 = new SystemRule();
        rule2.setHighestSystemLoad(2.3d);
        SystemRule rule3 = new SystemRule();
        rule3.setHighestSystemLoad(3.4d);
        SystemRuleManager.loadRules(Arrays.asList(rule1, rule2, rule3));

        List<SystemRule> rules = SystemRuleManager.getRules();
        assertEquals(1, rules.size());
        assertEquals(1.2d, rules.get(0).getHighestSystemLoad(), 0.01);
        assertEquals(1.2d, SystemRuleManager.getSystemLoadThreshold(), 0.01);
    }

    @Test
    public void testCheckMaxCpuUsageNotBBR() throws Exception {
        SystemRule rule1 = new SystemRule();
        rule1.setHighestCpuUsage(0d);
        SystemRuleManager.loadRules(Collections.singletonList(rule1));

        // Wait until SystemStatusListener triggered the first CPU usage collecting.
        Thread.sleep(1500);

        boolean blocked = false;
        try {
            StringResourceWrapper resourceWrapper = new StringResourceWrapper("testCheckMaxCpuUsageNotBBR", EntryType.IN);
            SystemRuleManager.checkSystem(resourceWrapper, 1);
        } catch (BlockException ex) {
            blocked = true;
        }
        assertTrue("The entry should be blocked under SystemRule maxCpuUsage=0", blocked);
    }

    @Before
    public void setUp() throws Exception {
        SystemRuleManager.loadRules(new ArrayList<SystemRule>());
    }

    @After
    public void tearDown() throws Exception {
        SystemRuleManager.loadRules(new ArrayList<SystemRule>());
    }
}
