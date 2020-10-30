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
package com.alibaba.csp.sentinel.slots.block.flow;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Weihua
 */
public class FlowRuleManagerTest {

    public static final List<FlowRule> STATIC_RULES_1 = new ArrayList<FlowRule>();
    public static final List<FlowRule> STATIC_RULES_2 = new ArrayList<FlowRule>();

    static {
        FlowRule first = new FlowRule();
        first.setResource("/a/b/c");
        first.setCount(100);
        STATIC_RULES_1.add(first);

        FlowRule second = new FlowRule();
        second.setResource("/a/b/c");
        second.setCount(200);
        STATIC_RULES_2.add(second);

    }

    @Test
    public void testLoadAndGetRules() throws InterruptedException{
        FlowRuleManager.loadRules(STATIC_RULES_1);
        assertEquals(1, FlowRuleManager.getRules().size()); // the initial size
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
                    FlowRuleManager.loadRules(i % 2 == 0 ? STATIC_RULES_2 : STATIC_RULES_1);
                }
                latchEnd.countDown();
            }
        }).start();

        latchStart.countDown();
        for (int i = 0; i < 10000; i++) {
            //The initial size is 1, and the size after updating should also be 1,
            //if the actual size is 0, that must be called after clear(),
            // but before putAll() in FlowPropertyListener.configUpdate
            assertEquals(1, FlowRuleManager.getRules().size());
        }
        latchEnd.await(10, TimeUnit.SECONDS);
    }
}
