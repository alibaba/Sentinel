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

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 * @author guozhong.huang
 */
public class SystemRuleManagerTest {

    @Test
    public void testLoadInvalidRules() {
        SystemRule rule1 = new SystemRule();
        rule1.setTriggerCount(-0.9d);
        rule1.setSystemMetricType(SystemMetricType.LOAD);
        SystemRule rule2 = new SystemRule();
        rule2.setTriggerCount(2.7d);
        rule2.setSystemMetricType(SystemMetricType.CPU_USAGE);
        SystemRuleManager.loadRules(Arrays.asList(rule1, rule2));
        assertEquals(0, SystemRuleManager.getRules().size());
    }

    @Test
    public void testLoadAndGetRules() {
        SystemRule rule1 = new SystemRule();
        rule1.setTriggerCount(1.2d);
        rule1.setSystemMetricType(SystemMetricType.LOAD);
        SystemRule rule2 = new SystemRule();
        rule2.setTriggerCount(17);
        rule2.setSystemMetricType(SystemMetricType.CONCURRENCY);
        SystemRule rule3 = new SystemRule();
        rule3.setTriggerCount(0.7d);
        rule3.setSystemMetricType(SystemMetricType.CPU_USAGE);
        SystemRule rule4 = new SystemRule();
        rule4.setTriggerCount(1500);
        rule4.setSystemMetricType(SystemMetricType.INBOUND_QPS);
        SystemRule rule5 = new SystemRule();
        rule5.setTriggerCount(50);
        rule5.setSystemMetricType(SystemMetricType.AVG_RT);
        SystemRuleManager.loadRules(Arrays.asList(rule1, rule2, rule3, rule4, rule5));

        List<SystemRule> rules = SystemRuleManager.getRules();

        for (SystemRule rule : rules) {
            switch (rule.getSystemMetricType()) {
                case LOAD:
                    assertEquals(1.2d, rule.getTriggerCount(), 0.01);
                    break;
                case CONCURRENCY:
                    assertEquals(17, rule.getTriggerCount(), 0.01);
                    break;
                case CPU_USAGE:
                    assertEquals(0.7d, rule.getTriggerCount(), 0.01);
                    break;
                case INBOUND_QPS:
                    assertEquals(1500, rule.getTriggerCount(), 0.01);
                    break;
                case AVG_RT:
                    assertEquals(50, rule.getTriggerCount(), 0.01);
                    break;
                default:
                    fail("Unexpected metric type");
            }
        }
    }

    @Test
    public void testLoadDuplicateTypeOfRules() {
        SystemRule rule1 = new SystemRule();
        rule1.setTriggerCount(1.2d);
        rule1.setSystemMetricType(SystemMetricType.LOAD);
        SystemRule rule2 = new SystemRule();
        rule2.setTriggerCount(2.3d);
        rule2.setSystemMetricType(SystemMetricType.LOAD);
        SystemRule rule3 = new SystemRule();
        rule3.setTriggerCount(3.4d);
        rule3.setSystemMetricType(SystemMetricType.LOAD);
        SystemRuleManager.loadRules(Arrays.asList(rule1, rule2, rule3));

        List<SystemRule> rules = SystemRuleManager.getRules();
        assertEquals(1, rules.size());
        assertEquals(1.2d, rules.get(0).getTriggerCount(), 0.01);
        assertEquals(1.2d, SystemRuleManager.getRules().get(0).getTriggerCount(), 0.01);
    }

    @Test
    public void testCheckMaxCpuUsageNotBBR() throws Exception {
        SystemRule rule1 = new SystemRule();
        rule1.setTriggerCount(0d);
        rule1.setSystemMetricType(SystemMetricType.CPU_USAGE);
        SystemRuleManager.loadRules(Collections.singletonList(rule1));

        // Wait until SystemStatusListener triggered the first CPU usage collecting.
        Thread.sleep(1500);

        boolean blocked = false;
        try {
            StringResourceWrapper resourceWrapper = new StringResourceWrapper("testCheckMaxCpuUsageNotBBR", EntryType.IN);
            SystemRuleChecker.checkSystem(SystemRuleManager.getRules(), resourceWrapper, 1);
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
