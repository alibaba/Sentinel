package com.alibaba.csp.sentinel.demo.degrade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * <p>
 * Degrade is used when the resources are in an unstable state, these resources
 * will be degraded within the next defined time window. There are three ways to
 * measure whether a resource is stable or not:
 * <ul>
 * <li>
 * Exception count: When the exception count in the last 60 seconds greats than
 * or equals to the threshold, access to the resource will be blocked in the
 * coming time window.
 * </li>
 * <li>
 * Exception ratio, see {@link ExceptionRatioDegradeDemo}.
 * </li>
 * <li>
 * For average response time, see {@link RtDegradeDemo}.
 * </li>
 * </ul>
 * </p>
 * <p>
 * Note: When degrading by {@link RuleConstant#DEGRADE_GRADE_EXCEPTION_COUNT}, time window
 * less than 60 seconds will not work as expected. Because the exception count is
 * summed by minute, when a short time window elapsed, the degradation condition
 * may still be satisfied.
 * </p>
 *
 * @author Carpenter Lee
 */
public class ExceptionCountDegradeDemo {
    private static final String KEY = "abc";

    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger bizException = new AtomicInteger();

    private static volatile boolean stop = false;
    private static final int threadCount = 1;
    private static int seconds = 60 + 40;

    public static void main(String[] args) throws Exception {
        tick();
        initDegradeRule();

        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    int count = 0;
                    while (true) {
                        count++;
                        Entry entry = null;
                        try {
                            Thread.sleep(20);
                            entry = SphU.entry(KEY);
                            // token acquired, means pass
                            pass.addAndGet(1);
                            if (count % 2 == 0) {
                                // biz code raise an exception.
                                throw new RuntimeException("throw runtime ");
                            }
                        } catch (BlockException e) {
                            block.addAndGet(1);
                        } catch (Throwable t) {
                            bizException.incrementAndGet();
                            Tracer.trace(t);
                        } finally {
                            total.addAndGet(1);
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
        // set limit exception count to 4
        rule.setCount(4);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        /**
         * When degrading by {@link RuleConstant#DEGRADE_GRADE_EXCEPTION_COUNT}, time window
         * less than 60 seconds will not work as expected. Because the exception count is
         * summed by minute, when a short time window elapsed, the degradation condition
         * may still be satisfied.
         */
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
            long oldBizException = 0;
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

                long globalBizException = bizException.get();
                long oneSecondBizException = globalBizException - oldBizException;
                oldBizException = globalBizException;

                System.out.println(TimeUtil.currentTimeMillis() + ", oneSecondTotal:" + oneSecondTotal
                    + ", oneSecondPass:" + oneSecondPass
                    + ", oneSecondBlock:" + oneSecondBlock
                    + ", oneSecondBizException:" + oneSecondBizException);
                if (seconds-- <= 0) {
                    stop = true;
                }
            }
            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total:" + total.get() + ", pass:" + pass.get()
                + ", block:" + block.get() + ", bizException:" + bizException.get());
            System.exit(0);
        }
    }
}
