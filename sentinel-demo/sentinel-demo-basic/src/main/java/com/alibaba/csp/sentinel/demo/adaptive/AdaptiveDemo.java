package com.alibaba.csp.sentinel.demo.adaptive;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.demo.flow.FlowQpsDemo;
import com.alibaba.csp.sentinel.slots.adaptive.AdaptiveRule;
import com.alibaba.csp.sentinel.slots.adaptive.AdaptiveRuleManager;
import com.alibaba.csp.sentinel.slots.adaptive.algorithm.BRPCLimit;
import com.alibaba.csp.sentinel.slots.adaptive.algorithm.GradientLimit;
import com.alibaba.csp.sentinel.slots.adaptive.algorithm.VegasLimit;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ElonTusk
 * @name AdaptiveDemo
 * @date 2023/8/7 16:08
 */
public class AdaptiveDemo {
    private static final String KEY = "abc";

    public static void main(String[] args) {
        initAdaptiveRule();

        tick();
        // first make the system run on a very low condition
        simulateTraffic();

        System.out.println("===== begin to do flow control");

    }

    private static void initAdaptiveRule() {
        List<AdaptiveRule> rules = new ArrayList<>();
        AdaptiveRule rule1 = new AdaptiveRule();
        rule1.setResource(KEY);
        // set init limit qp
        // init count 20
        int initCount = 10;
        rule1.setCount(initCount);
        rule1.addCount(initCount);
//        rule1.setStrategy(RuleConstant.ADAPTIVE_VEGAS);
//        rule1.setLimiter(VegasLimit.getInstance());
//        rule1.setStrategy(RuleConstant.ADAPTIVE_GRADIENT);
//        rule1.setLimiter(GradientLimit.getInstance());
        rule1.setStrategy(RuleConstant.ADAPTIVE_BRPC);
        rule1.setLimiter(BRPCLimit.getInstance());
        rules.add(rule1);
        AdaptiveRuleManager.loadRules(rules);

        List<FlowRule> rules2 = new ArrayList<>();
        FlowRule rule2 = new FlowRule();
        rule2.setResource(KEY);
        rule2.setCount(initCount);
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setLimitApp("default");
        rules2.add(rule2);
        FlowRuleManager.loadRules(rules2);
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    private static volatile boolean stop = false;
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static final int threadCount = 10;

    private static int seconds = 60 + 40;

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

                System.out.println(seconds + " send qps is: " + oneSecondTotal);
                System.out.println(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                        + ", pass:" + oneSecondPass
                        + ", block:" + oneSecondBlock);

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

    private static void simulateTraffic() {
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask());
            t.setName("simulate-traffic-Task");
            t.start();
        }
    }

    static Random random = new Random();

    static class RunTask implements Runnable {
        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;

                try {
                    entry = SphU.entry(KEY, EntryType.IN);
                    // token acquired, means pass
                    try {
                        TimeUnit.MILLISECONDS.sleep(90 + random.nextInt(20));
                        //TimeUnit.MILLISECONDS.sleep(50);
                        //CpuRunMethod();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    pass.addAndGet(1);

                } catch (BlockException e1) {
                    block.incrementAndGet();
                    try {
                        //TimeUnit.MILLISECONDS.sleep(random.nextInt(500));
                        TimeUnit.MILLISECONDS.sleep(0);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                } catch (Exception e2) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }

                Random random2 = new Random();
                try {
                    //TimeUnit.MILLISECONDS.sleep(random2.nextInt(100));
                    TimeUnit.MILLISECONDS.sleep(0);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }
}
