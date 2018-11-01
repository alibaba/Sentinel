package com.alibaba.csp.sentinel.demo.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * When {@link FlowRule#controlBehavior} set to {@link RuleConstant#CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER}, real passed
 * qps will gradually increase to {@link FlowRule#count}, other than burst increasing, and after the passed qps reaches
 * the threshold, the request will pass at a constant interval.
 * <p>
 * In short, {@link RuleConstant#CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER} behaves like
 * {@link RuleConstant#CONTROL_BEHAVIOR_WARM_UP} + {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER}.
 * </p>
 *
 * <p/>
 * Run this demo, results are as follows:
 * <pre>
 * ...
 * 1541035848056, total:5, pass:5, block:0 // run in slow qps
 * 1541035849061, total:0, pass:0, block:0
 * 1541035850066, total:6, pass:6, block:0
 * 1541035851068, total:2, pass:2, block:0
 * 1541035852073, total:3, pass:3, block:0
 * 1541035853078, total:3361, pass:7, block:3354 // request qps burst increase, warm up behavior triggered.
 * 1541035854083, total:3414, pass:7, block:3407
 * 1541035855087, total:3377, pass:7, block:3370
 * 1541035856091, total:3366, pass:8, block:3358
 * 1541035857096, total:3259, pass:8, block:3251
 * 1541035858101, total:3066, pass:13, block:3054
 * 1541035859105, total:3042, pass:15, block:3026
 * 1541035860109, total:2946, pass:17, block:2929
 * 1541035861113, total:2909, pass:20, block:2889 // warm up process end, pass qps increased to {@link FlowRule#count}
 * 1541035862117, total:2970, pass:20, block:2950
 * 1541035863122, total:2919, pass:20, block:2899
 * 1541035864127, total:2903, pass:21, block:2882
 * 1541035865133, total:2930, pass:20, block:2910
 * ...
 * </pre>
 *
 * @author CarpenterLee
 * @see WarmUpFlowDemo
 * @see PaceFlowDemo
 */
public class WarmUpRateLimiterFlowDemo {
    private static final String KEY = "abc";

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static volatile boolean stop = false;

    private static final int threadCount = 100;
    private static int seconds = 100;

    public static void main(String[] args) throws Exception {
        initFlowRule();
        // trigger Sentinel internal init
        Entry entry = null;
        try {
            entry = SphU.entry(KEY);
        } catch (Exception e) {
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }

        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();

        //first make the system run on a very low condition
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new SlowTask());
            t.setName("sentinel-slow-task");
            t.start();
        }
        Thread.sleep(5000);

        // request qps burst increase, warm up behavior triggered.
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask());
            t.setName("sentinel-run-task");
            t.start();
        }
    }

    private static void initFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        rule1.setCount(20);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER);
        rule1.setWarmUpPeriodSec(10);
        rule1.setMaxQueueingTimeMs(100);

        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    static class SlowTask implements Runnable {
        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    // token acquired, means pass
                    pass.addAndGet(1);
                } catch (BlockException e1) {
                    block.incrementAndGet();
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
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(2000));
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    static class RunTask implements Runnable {
        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;
                try {
                    entry = SphU.entry(KEY);
                    pass.addAndGet(1);
                } catch (BlockException e1) {
                    block.incrementAndGet();
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
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(50));
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
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
}
