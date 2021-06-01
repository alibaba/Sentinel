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
package com.alibaba.csp.sentinel.demo.degrade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * Run this demo, and the output will be like:
 *
 * <pre>
 * 1529399827825,total:0, pass:0, block:0
 * 1529399828825,total:4263, pass:100, block:4164
 * 1529399829825,total:19179, pass:4, block:19176 // circuit breaker opens
 * 1529399830824,total:19806, pass:0, block:19806
 * 1529399831825,total:19198, pass:0, block:19198
 * 1529399832824,total:19481, pass:0, block:19481
 * 1529399833826,total:19241, pass:0, block:19241
 * 1529399834826,total:17276, pass:0, block:17276
 * 1529399835826,total:18722, pass:0, block:18722
 * 1529399836826,total:19490, pass:0, block:19492
 * 1529399837828,total:19355, pass:0, block:19355
 * 1529399838827,total:11388, pass:0, block:11388
 * 1529399839829,total:14494, pass:104, block:14390 // After 10 seconds, the system restored
 * 1529399840854,total:18505, pass:0, block:18505
 * 1529399841854,total:19673, pass:0, block:19676
 * </pre>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class SlowRatioCircuitBreakerDemo {

    private static final String KEY = "some_method";

    private static volatile boolean stop = false;
    private static int seconds = 120;

    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        initDegradeRule();
        registerStateChangeObserver();
        startTick();

        int concurrency = 8;
        for (int i = 0; i < concurrency; i++) {
            Thread entryThread = new Thread(() -> {
                while (true) {
                    Entry entry = null;
                    try {
                        entry = SphU.entry(KEY);
                        pass.incrementAndGet();
                        // RT: [40ms, 60ms)
                        sleep(ThreadLocalRandom.current().nextInt(40, 60));
                    } catch (BlockException e) {
                        block.incrementAndGet();
                        sleep(ThreadLocalRandom.current().nextInt(5, 10));
                    } finally {
                        total.incrementAndGet();
                        if (entry != null) {
                            entry.exit();
                        }
                    }
                }
            });
            entryThread.setName("sentinel-simulate-traffic-task-" + i);
            entryThread.start();
        }
    }

    private static void registerStateChangeObserver() {
        EventObserverRegistry.getInstance().addStateChangeObserver("logging",
            (prevState, newState, rule, snapshotValue) -> {
                if (newState == State.OPEN) {
                    System.err.println(String.format("%s -> OPEN at %d, snapshotValue=%.2f", prevState.name(),
                        TimeUtil.currentTimeMillis(), snapshotValue));
                } else {
                    System.err.println(String.format("%s -> %s at %d", prevState.name(), newState.name(),
                        TimeUtil.currentTimeMillis()));
                }
            });
    }

    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule(KEY)
            .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
            // Max allowed response time
            .setCount(50)
            // Retry timeout (in second)
            .setTimeWindow(10)
            // Circuit breaker opens when slow request ratio > 60%
            .setSlowRatioThreshold(0.6)
            .setMinRequestAmount(100)
            .setStatIntervalMs(20000);
        rules.add(rule);

        DegradeRuleManager.loadRules(rules);
        System.out.println("Degrade rule loaded: " + rules);
    }

    private static void sleep(int timeMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeMs);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private static void startTick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-tick-task");
        timer.start();
    }

    static class TimerTask implements Runnable {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("Begin to run! Go go go!");
            System.out.println("See corresponding metrics.log for accurate statistic data");

            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;

            while (!stop) {
                sleep(1000);

                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                System.out.println(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                    + ", pass:" + oneSecondPass + ", block:" + oneSecondBlock);

                if (seconds-- <= 0) {
                    stop = true;
                }
            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total: " + total.get() + ", pass:" + pass.get()
                + ", block:" + block.get());
            System.exit(0);
        }
    }
}
