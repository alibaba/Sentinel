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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    @Test
    public void appendAndReplaceRules(){
        FlowRuleManager.loadRules(STATIC_RULES_1);
        //replace
        FlowRuleManager.appendAndReplaceRules(STATIC_RULES_2);
        assertEquals(1, FlowRuleManager.getRules().size());
        //append
        FlowRuleManager.appendAndReplaceRules(Collections.singletonList(new FlowRule("test")
        .setCount(10d)
        ));
        assertEquals(2, FlowRuleManager.getRules().size());

        FlowRule diff_limit_app_rule = new FlowRule("test")
                .setCount(10d);
        diff_limit_app_rule.setLimitApp("testapp");
        FlowRuleManager.appendAndReplaceRules(Collections.singletonList(diff_limit_app_rule));
        assertEquals(3, FlowRuleManager.getRules().size());

    }
    @Test
    public void deleteRules(){
        FlowRuleManager.loadRules(STATIC_RULES_1);
        //delete not exists
        FlowRuleManager.deleteRules(Collections.singletonList(new FlowRule(
                "not_exist")));
        assertEquals(1, FlowRuleManager.getRules().size());
        FlowRule diff_limit_app_rule = new FlowRule(STATIC_RULES_1.get(0).getResource())
                .setCount(10d);
        diff_limit_app_rule.setLimitApp("different");
        FlowRuleManager.deleteRules(Collections.singletonList(diff_limit_app_rule));
        assertEquals(1, FlowRuleManager.getRules().size());
        FlowRuleManager.deleteRules(STATIC_RULES_1);
        assertEquals(0, FlowRuleManager.getRules().size());
    }
    @Test
    public void multiChangeRules() throws InterruptedException, ExecutionException, TimeoutException {
        FlowRuleManager.loadRules(STATIC_RULES_1);
        AtomicBoolean isMeet = new AtomicBoolean(true);
        int  times = 1000;
        int singleTimes = 1;
        CompletableFuture cf1 = CompletableFuture.runAsync(()->{

            for(int i = 0; i < times; i++){
                FlowRuleManager.appendAndReplaceRules(STATIC_RULES_1);
            }
        });
        CompletableFuture cf2 = CompletableFuture.runAsync(()->{
            boolean tmpMatch = true;

            for(int i = 0; i < times; i++){
                tmpMatch &= FlowRuleManager.appendAndReplaceRules(Collections.singletonList(new FlowRule("test"+i)));
            }
            if(!tmpMatch){
                isMeet.set(tmpMatch);
            }


        });

        CompletableFuture all = CompletableFuture.allOf(cf1,cf2);
        all.get(10,TimeUnit.MINUTES);
        assertEquals(times+singleTimes,FlowRuleManager.getRules().size());
//        assertTrue(!isMeet.get());

    }

}
