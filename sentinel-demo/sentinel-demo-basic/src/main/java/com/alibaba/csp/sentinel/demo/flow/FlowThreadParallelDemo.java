package com.alibaba.csp.sentinel.demo.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

public class FlowThreadParallelDemo {
    
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger entryThread = new AtomicInteger();
    private static AtomicInteger parallelThread = new AtomicInteger();
    
    final static String resource = "echo";
    final static int threadCount = 200;
    
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        
        initFlowRule();
        
        CountDownLatch latch = new CountDownLatch(threadCount);
        long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        Runnable runner = () -> {
            while (nextLoop(timeout)) {
                Entry entry = null;
                try {
                    entry = SphU.entry(resource);
                    resync(entryThread.incrementAndGet());
                    TimeUnit.MILLISECONDS.sleep(5);
                    pass.incrementAndGet();
                } catch (BlockException e) {
                    block.incrementAndGet();
                } catch (Exception e) {
                    // biz exception
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entryThread.decrementAndGet();
                        entry.exit();
                    }
                }
            }
            latch.countDown();
        };
        
        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(runner);
            entryThread.setName("working thread");
            entryThread.start();
        }
        latch.await();
        
        long cost = System.currentTimeMillis() - start; 
        System.out.println("time cost: " + cost + " ms");
        System.out.println("total:" + total.get() + ", pass:" + pass.get()
            + ", block:" + block.get() + ", parallel:" + parallelThread.get());
    }
    
    private static boolean nextLoop(long timeout) {
        Thread.yield();
        return System.currentTimeMillis() <= timeout;
    }

    private static void initFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        rule.setCount(10);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }
    
    private static void resync(int entryThread) {
        int parallel;
        do {
            parallel = parallelThread.get();
        } while (parallel < entryThread && !parallelThread.compareAndSet(parallel, entryThread));
    };
    
}
