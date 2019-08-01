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
package com.alibaba.csp.sentinel.slots.block.global;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_THREAD;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author lianglin
 * @since 2019-07-31 18:11
 */
public class GlobalRuleManagerTest {

    private String globalResource = SentinelConfig.getConfig(SentinelConfig.GLOBAL_RULE_RESOURCE_NAME);

    @Before
    public void setUp() throws Exception {
        SentinelConfig.setConfig(SentinelConfig.GLOBAL_RULE_SWITCH, "true");
        GlobalRuleManager.loadRules(new ArrayList<AbstractRule>());
        FlowRuleManager.loadRules(new ArrayList<FlowRule>());
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    @After
    public void tearDown() throws Exception {
        GlobalRuleManager.loadRules(new ArrayList<AbstractRule>());
        FlowRuleManager.loadRules(new ArrayList<FlowRule>());
        DegradeRuleManager.loadRules(new ArrayList<DegradeRule>());
    }

    private void initGlobalFlow(List<AbstractRule> globalRules) {
        int rtSlowRequestAmount = 10;
        DegradeRule rule = new DegradeRule();
        rule.setCount(1);
        rule.setResource(globalResource);
        rule.setTimeWindow(2);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rule.setRtSlowRequestAmount(rtSlowRequestAmount);
        globalRules.add(rule);
    }

    private void initGlobalDegrade(List<AbstractRule> globalRules) {
        FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(1);
        flowRule.setLimitApp("default");
        flowRule.setResource(globalResource);
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        globalRules.add(flowRule);
    }


    @Test
    public void testLoad() {

        List<AbstractRule> globalRules = new ArrayList<>();
        initGlobalFlow(globalRules);
        initGlobalDegrade(globalRules);
        GlobalRuleManager.loadRules(globalRules);
        Assert.assertTrue(GlobalRuleManager.getRules() != null);
        Assert.assertTrue(GlobalRuleManager.getRule(GlobalRuleType.FLOW) != null);
        Assert.assertTrue(GlobalRuleManager.getRule(GlobalRuleType.DEGRADE) != null);

    }


    @Test
    public void testGlobalFlowQPS() {

        FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(1);
        flowRule.setLimitApp("default");
        flowRule.setResource(globalResource);
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);

        List<AbstractRule> globalRules = new ArrayList<>();
        globalRules.add(flowRule);

        GlobalRuleManager.loadRules(globalRules);

        Entry e = null;
        try {
            e = SphU.entry("testQPSGrade");
        } catch (BlockException e1) {
            assertTrue(false);
        }
        e.exit();
        try {
            e = SphU.entry("testQPSGrade");
            assertTrue(false);
        } catch (BlockException e1) {
            assertTrue(true);
        }
        e.exit();
    }

    @Test(expected = BlockException.class)
    public void testFlowThreadGrade() throws InterruptedException, BlockException {

        FlowRule flowRule = new FlowRule();
        flowRule.setResource(globalResource);
        flowRule.setGrade(FLOW_GRADE_THREAD);
        flowRule.setCount(1);

        List<AbstractRule> globalRules = new ArrayList<>();
        globalRules.add(flowRule);
        GlobalRuleManager.loadRules(globalRules);

        final Object sequence = new Object();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Entry e = null;
                try {
                    e = SphU.entry("testThreadGrade");
                    synchronized (sequence) {
                        System.out.println("notify up");
                        sequence.notify();
                    }
                    Thread.sleep(100);
                } catch (BlockException e1) {
                    fail("Should had failed");
                } catch (InterruptedException e1) {
                    fail("Should had failed");
                }
                e.exit();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

        synchronized (sequence) {
            System.out.println("sleep");
            sequence.wait();
            System.out.println("wake up");
        }

        SphU.entry("testThreadGrade");
        System.out.println("done");
    }


    @Test
    public void testCustomAndGlobalFlow() {

        //custom flow rules:  limit qps
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("testFlow");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(10);
        flowRule.setLimitApp("default");
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        FlowRuleManager.loadRules(Arrays.asList(flowRule));

        //global flow rule
        final FlowRule globalFlowRule = new FlowRule();
        globalFlowRule.setResource(globalResource);
        globalFlowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        globalFlowRule.setCount(50);
        globalFlowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        List<AbstractRule> globalRules = new ArrayList<>();
        globalRules.add(globalFlowRule);
        GlobalRuleManager.loadRules(globalRules);

        Entry e = null;
        try {
            for (int i = 0; i < 8; i++) {
                e = SphU.entry("testFlow");
                Assert.assertTrue(true);
            }
        } catch (BlockException ex) {
            //global flow rule valid
            ex.printStackTrace();
            Assert.assertTrue(ex.getRule() == globalFlowRule);
        }

        globalFlowRule.setCount(20);
        globalRules.clear();
        globalRules.add(globalFlowRule);
        GlobalRuleManager.loadRules(globalRules);
        try {
            for (int i = 0; i < 20; i++) {
                e = SphU.entry("testFlow");
                Assert.assertTrue(true);
            }
        } catch (BlockException ex) {
            //custom flow rule valid
            ex.printStackTrace();
            Assert.assertTrue(ex.getRule() == flowRule);
        }


    }


    @Test
    public void testDegrade() throws InterruptedException {

        List<AbstractRule> rules = new ArrayList<AbstractRule>();

        int exceptionCount = 0;
        double rationHold = 0.1;
        DegradeRule globalDegradeRule = new DegradeRule();
        globalDegradeRule.setResource(globalResource);
        // set limit exception ratio to 0.1
        globalDegradeRule.setCount(rationHold);
        globalDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        globalDegradeRule.setTimeWindow(1);
        globalDegradeRule.setMinRequestAmount(20);
        rules.add(globalDegradeRule);
        GlobalRuleManager.loadRules(rules);

        Entry entry = null;
        int count = 30;
        for (int i = 0; i < count; i++) {
            try {
                entry = SphU.entry("testDegrade");
                if (i % 2 == 0) {
                    throw new RuntimeException("exception");
                }
            } catch (BlockException ex) {
                exceptionCount++;
                Assert.assertTrue(ex.getRule() == globalDegradeRule);
            } catch (Throwable throwable) {
                Tracer.trace(throwable);
            } finally {
                if (entry != null) {
                    entry.exit();
                }
            }

        }
        Assert.assertTrue(exceptionCount > 0);
        Assert.assertTrue((exceptionCount * 1.0 / count) >= rationHold);


        Thread.sleep(1100);
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource("testDegrade");
        degradeRule.setCount(1.0);
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        degradeRule.setTimeWindow(10);
        degradeRule.setMinRequestAmount(1);
        DegradeRuleManager.loadRules(Arrays.asList(degradeRule));

        for (int i = 0; i < 3; i++) {
            try {
                entry = SphU.entry("testDegrade");
                Thread.sleep(1100);
            } catch (BlockException e) {
                e.printStackTrace();
                assertTrue(e.getRule() == degradeRule);
            }
        }


    }


}
