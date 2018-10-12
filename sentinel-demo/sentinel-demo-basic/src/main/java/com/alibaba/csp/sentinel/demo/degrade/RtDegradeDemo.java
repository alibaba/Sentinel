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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;

/**
 * <p>
 * Degrade is used when the resources are in an unstable state, these resources
 * will be degraded within the next defined time window. There are two ways to
 * measure whether a resource is stable or not:
 * <ul>
 * <li>
 * Average Response Time ('DegradeRule.Grade=RuleContants.DEGRADE_GRADE_RT'): When the
 * average RT greats than or equals to the threshold ('count' in 'DegradeRule', ms), the
 * resource enters a quasi-degraded state. If the RT of next coming five requests still
 * exceed this threshold, this resource will be downgraded, which means that in
 * the next time window(Defined in 'timeWindow', s units) all the access to this
 * resource will be blocked.
 * </li>
 * <li>
 * Exception ratio, see {@link ExceptionRatioDegradeDemo}.
 * </li>
 * <li>
 * Exception Count, see {@link ExceptionCountDegradeDemo}.
 * </li>
 * </ul>
 *
 * </p>
 *
 * Run this demo, and the out put will be like:
 *
 * <pre>
 * 1529399827825,total:0, pass:0, block:0
 * 1529399828825,total:4263, pass:100, block:4164
 * 1529399829825,total:19179, pass:4, block:19176
 * 1529399830824,total:19806, pass:0, block:19806  //begin degrade
 * 1529399831825,total:19198, pass:0, block:19198
 * 1529399832824,total:19481, pass:0, block:19481
 * 1529399833826,total:19241, pass:0, block:19241
 * 1529399834826,total:17276, pass:0, block:17276
 * 1529399835826,total:18722, pass:0, block:18722
 * 1529399836826,total:19490, pass:0, block:19492
 * 1529399837828,total:19355, pass:0, block:19355
 * 1529399838827,total:11388, pass:0, block:11388
 * 1529399839829,total:14494, pass:104, block:14390 //After 10 seconds, the system is restored, and degraded very
 * quickly
 * 1529399840854,total:18505, pass:0, block:18505
 * 1529399841854,total:19673, pass:0, block:19676
 * </pre>
 *
 * @author jialiang.linjl
 */
public class RtDegradeDemo {

    private static final String KEY = "abc";

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static volatile boolean stop = false;
    private static final int threadCount = 100;
    private static int seconds = 60 + 40;

    public static void main(String[] args) throws Exception {

        tick();
        initDegradeRule();

        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        Entry entry = null;
                        try {
                            TimeUnit.MILLISECONDS.sleep(5);
                            entry = SphU.entry(KEY);
                            // token acquired
                            pass.incrementAndGet();
                            // sleep 600 ms, as rt
                            TimeUnit.MILLISECONDS.sleep(600);
                        } catch (Exception e) {
                            block.incrementAndGet();
                        } finally {
                            total.incrementAndGet();
                            if (entry != null) {
                                entry.exit();
                            }
                        }
                    }
                }

            });
            entryThread.setName("working-thread");
            entryThread.start();
        }
    }

    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<DegradeRule>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(KEY);
        // set threshold rt, 10 ms
        rule.setCount(10);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rule.setTimeWindow(10);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    static class TimerTask implements Runnable {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("begin to statistic!!!");
            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;

            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }

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
            System.out.println("total:" + total.get() + ", pass:" + pass.get()
                + ", block:" + block.get());
            System.exit(0);
        }
    }

}
