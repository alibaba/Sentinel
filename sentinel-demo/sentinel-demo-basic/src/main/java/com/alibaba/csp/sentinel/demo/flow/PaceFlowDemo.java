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
package com.alibaba.csp.sentinel.demo.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

/**
 * <p>
 * If {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER} is set, incoming
 * requests are passing at regular interval. When a new request arrives, the
 * flow rule checks whether the interval between the new request and the
 * previous request. If the interval is less than the count set in the rule
 * first. If the interval is large, it will pass the request; otherwise,
 * sentinel will calculate the waiting time for this request. If the waiting
 * time is longer than the {@link FlowRule#maxQueueingTimeMs} set in the rule,
 * the request will be rejected immediately.
 *
 * This method is widely used for pulsed flow. When a large amount of flow
 * comes, we don't want to pass all these requests at once, which may drag the
 * system down. We can make the system handle these requests at a steady pace by
 * using this kind of rules.
 *
 * <p>
 * This demo demonstrates how to use {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER}.
 * </p>
 *
 * <p>
 * {@link #initPaceFlowRule() } create rules that uses
 * {@code CONTROL_BEHAVIOR_RATE_LIMITER}.
 * <p>
 * {@link #simulatePulseFlow()} simulates 100 requests that arrives at almost the
 * same time. All these 100 request are passed at a fixed interval.
 *
 * <p/>
 * Run this demo, results are as follows:
 * <pre>
 * pace behavior
 * ....
 * 1528872403887 one request pass, cost 9348 ms // every 100 ms pass one request.
 * 1528872403986 one request pass, cost 9469 ms
 * 1528872404087 one request pass, cost 9570 ms
 * 1528872404187 one request pass, cost 9642 ms
 * 1528872404287 one request pass, cost 9770 ms
 * 1528872404387 one request pass, cost 9848 ms
 * 1528872404487 one request pass, cost 9970 ms
 * ...
 * done
 * total pass:100, total block:0
 * </pre>
 *
 * Then we invoke {@link #initDefaultFlowRule()} to set rules with default behavior, and only 10
 * requests will be allowed to pass, other requests will be rejected immediately.
 * <p/>
 * The output will be like:
 * <pre>
 * default behavior
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101280 one request pass, cost 1 ms
 * 1530500101280 one request pass, cost 0 ms
 * 1530500101280 one request pass, cost 0 ms
 * 1530500101280 one request pass, cost 0 ms
 * done
 * total pass:10, total block:90 // 10 requests passed, other 90 requests rejected immediately.
 * </pre>
 *
 * @author jialiang.linjl
 */
public class PaceFlowDemo {

    private static final String KEY = "abc";

    private static volatile CountDownLatch countDown;

    private static final Integer requestQps = 100;
    private static final Integer count = 10;
    private static final AtomicInteger done = new AtomicInteger();
    private static final AtomicInteger pass = new AtomicInteger();
    private static final AtomicInteger block = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("pace behavior");
        countDown = new CountDownLatch(1);
        initPaceFlowRule();
        simulatePulseFlow();
        countDown.await();

        System.out.println("done");
        System.out.println("total pass:" + pass.get() + ", total block:" + block.get());

        System.out.println();
        System.out.println("default behavior");
        TimeUnit.SECONDS.sleep(5);
        done.set(0);
        pass.set(0);
        block.set(0);
        countDown = new CountDownLatch(1);
        initDefaultFlowRule();
        simulatePulseFlow();
        countDown.await();
        System.out.println("done");
        System.out.println("total pass:" + pass.get() + ", total block:" + block.get());
        System.exit(0);
    }

    private static void initPaceFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        rule1.setCount(count);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        /*
         * CONTROL_BEHAVIOR_RATE_LIMITER means requests more than threshold will be queueing in the queue,
         * until the queueing time is more than {@link FlowRule#maxQueueingTimeMs}, the requests will be rejected.
         */
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        rule1.setMaxQueueingTimeMs(20 * 1000);

        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    private static void initDefaultFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        rule1.setCount(count);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        // CONTROL_BEHAVIOR_DEFAULT means requests more than threshold will be rejected immediately.
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    private static void simulatePulseFlow() {
        for (int i = 0; i < requestQps; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime = TimeUtil.currentTimeMillis();
                    Entry entry = null;
                    try {
                        entry = SphU.entry(KEY);
                    } catch (BlockException e1) {
                        block.incrementAndGet();
                    } catch (Exception e2) {
                        // biz exception
                    } finally {
                        if (entry != null) {
                            entry.exit();
                            pass.incrementAndGet();
                            long cost = TimeUtil.currentTimeMillis() - startTime;
                            System.out.println(
                                TimeUtil.currentTimeMillis() + " one request pass, cost " + cost + " ms");
                        }
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(5);
                    } catch (InterruptedException e1) {
                        // ignore
                    }

                    if (done.incrementAndGet() >= requestQps) {
                        countDown.countDown();
                    }
                }
            }, "Thread " + i);
            thread.start();
        }
    }
}
