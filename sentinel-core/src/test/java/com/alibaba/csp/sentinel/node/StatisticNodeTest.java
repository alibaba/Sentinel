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
package com.alibaba.csp.sentinel.node;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for {@link StatisticNode}.
 *
 * @author cdfive
 */
public class StatisticNodeTest {

    private static final String LOG_PREFIX = "[StatisticNodeTest] ";

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-HH-dd HH:mm:ss");

    private static final Random RANDOM = new Random();

    private static final int THREAD_COUNT = 20;

    /**
     * A simple test for statistic threadNum and qps by using StatisticNode
     *
     * <p>
     * 20 threads, 30 tasks, every task execute 10 times of bizMethod
     * one bizMethod execute within 1 second, and within 0.5 second interval to exceute next bizMthod
     * so that the total time cost will be within 1 minute
     * </p>
     *
     * <p>
     * Print the statistic info of StatisticNode and verify some results
     * </p>
     */
    @Test
    public void testStatisticThreadNumAndQps() {
        long testStartTime = TimeUtil.currentTimeMillis();

        int taskCount = 30;
        int taskBizExecuteCount = 10;

        StatisticNode node = new StatisticNode();

        ExecutorService bizEs = Executors.newFixedThreadPool(THREAD_COUNT);
        ExecutorService tickEs = Executors.newSingleThreadExecutor();

        tickEs.submit(new TickTask(node));

        List<BizTask> bizTasks = new ArrayList<>(taskBizExecuteCount);
        for (int i = 0; i < taskCount; i++) {
            bizTasks.add(new BizTask(node, taskBizExecuteCount));
        }
        try {
            bizEs.invokeAll(bizTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log("====================================================");
        log("all biz task done, waiting 3 second to exit");
        sleep(3000);

        bizEs.shutdownNow();
        tickEs.shutdownNow();

        // now no biz method execute, so there is no curThreadNum,passQps,successQps
        assertEquals(0, node.curThreadNum(), 0.01);
        assertEquals(0, node.passQps(), 0.01);
        assertEquals(0, node.successQps(), 0.01);

        // note: total time cost should be controlled within 1 minute,
        // as the node.totalRequest() holding statistics of recent 60 seconds
        int totalRequest = taskCount * taskBizExecuteCount;
        // verify totalRequest
        assertEquals(totalRequest, node.totalRequest());
        // as all execute success, totalRequest should equal to totalSuccess
        assertEquals(totalRequest, node.totalSuccess());

        // now there are no data in time span, so the minRT should be equals to TIME_DROP_VALVE
        assertEquals(node.minRt(), Constants.TIME_DROP_VALVE, 0.01);

        log("====================================================");
        log("testStatisticThreadNumAndQps done, cost " + (TimeUtil.currentTimeMillis() - testStartTime) + "ms");
    }

    private static class BizTask implements Callable<Object> {

        private StatisticNode node;

        private Integer bizExecuteCount;

        public BizTask(StatisticNode node, Integer bizExecuteCount) {
            this.node = node;
            this.bizExecuteCount = bizExecuteCount;
        }

        @Override
        public Object call() throws Exception {
            while (true) {
                node.increaseThreadNum();
                node.addPassRequest(1);

                long startTime = TimeUtil.currentTimeMillis();
                bizMethod();

                node.decreaseThreadNum();

                // decrease one ThreadNum, so curThreadNum should less than THREAD_COUNT
                assertTrue(node.curThreadNum() < THREAD_COUNT);

                long rt = TimeUtil.currentTimeMillis() - startTime;
                node.addRtAndSuccess(rt, 1);

                // wait random 0.5 second for simulate method call interval,
                // otherwise the curThreadNum will always be THREAD_COUNT at the beginning
                sleep(RANDOM.nextInt(500));

                bizExecuteCount--;
                if (bizExecuteCount <= 0) {
                    break;
                }
            }

            return null;
        }
    }

    private static void bizMethod() {
        // simulate biz method call in random 1 second
        sleep(RANDOM.nextInt(1000));
    }

    private static class TickTask implements Runnable {

        private StatisticNode node;

        public TickTask(StatisticNode node) {
            this.node = node;
        }

        @Override
        public void run() {
            while (true) {
                // print statistic info every 1 second
                sleep(1000);

                // the curThreadNum should not greater than THREAD_COUNT
                assertTrue(node.curThreadNum() <= THREAD_COUNT);

                logNode(node);
            }
        }
    }

    private static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void logNode(StatisticNode node) {
        log(SDF.format(new Date()) + " curThreadNum=" + node.curThreadNum() + ",passQps=" + node.passQps()
                + ",successQps=" + node.successQps() + ",maxSuccessQps=" + node.maxSuccessQps()
                + ",totalRequest=" + node.totalRequest() + ",totalSuccess=" + node.totalSuccess()
                + ", avgRt=" + String.format("%.2f", node.avgRt()) + ", minRt=" + node.minRt());
    }

    private static void log(Object obj) {
        System.out.println(LOG_PREFIX + obj);
    }


    /**
     * com.alibaba.csp.sentinel.node.StatisticNode#curThreadNum using LongAdder replace the  AtomicInteger.
     * now test the LongAdder is fast than AtomicInteger
     * and get the right statistic or not
     */
    @Test
    public void testStatisticLongAdder() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        StatisticNode statisticNode = new StatisticNode();
        ExecutorService bizEs1 = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        ExecutorService bizEs2 = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        int taskCount = 100;
        for (int i = 0; i < taskCount; i++) {
            int op = i % 2;
            bizEs2.submit(new StatisticAtomicIntegerTask(atomicInteger, op, i));
            bizEs1.submit(new StatisticLongAdderTask(statisticNode, op, i));
        }
        Thread.sleep(5000);

        log("LongAdder totalCost : " + StatisticLongAdderTask.totalCost() + "ms");
        log("AtomicInteger totalCost : " + StatisticAtomicIntegerTask.totalCost() + "ms");
        Assert.assertEquals(statisticNode.curThreadNum(), atomicInteger.get());


    }

    private static class StatisticLongAdderTask implements Runnable {


        private StatisticNode statisticNode;
        /**
         * 0 addition
         * 1 subtraction
         */
        private int op;

        private int taskId;

        private static Map<Integer, Long> taskCostMap = new ConcurrentHashMap<>(16);


        public StatisticLongAdderTask(StatisticNode statisticNode, int op, int taskId) {
            this.statisticNode = statisticNode;
            this.op = op;
            this.taskId = taskId;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            int calCount = 100000;
            for (int i = 0; i < calCount; i++) {
                if (op == 0) {
                    statisticNode.increaseThreadNum();
                } else if (op == 1) {
                    statisticNode.decreaseThreadNum();
                }
            }
            long cost = System.currentTimeMillis() - startTime;
            taskCostMap.put(taskId, cost);
        }

        public static long totalCost() {
            long totalCost = 0;
            for (long cost : taskCostMap.values()) {
                totalCost += cost;
            }
            return totalCost;
        }
    }

    private static class StatisticAtomicIntegerTask implements Runnable {

        AtomicInteger atomicInteger;
        /**
         * 0 addition
         * 1 subtraction
         */
        private int op;

        private int taskId;

        private static Map<Integer, Long> taskCostMap = new ConcurrentHashMap<>(16);

        public StatisticAtomicIntegerTask(AtomicInteger atomicInteger, int op, int taskId) {
            this.atomicInteger = atomicInteger;
            this.op = op;
            this.taskId = taskId;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            int calCount = 100000;
            for (int i = 0; i < calCount; i++) {
                if (op == 0) {
                    atomicInteger.incrementAndGet();
                } else if (op == 1) {
                    atomicInteger.decrementAndGet();
                }
            }
            long cost = System.currentTimeMillis() - startTime;
            taskCostMap.put(taskId, cost);
        }

        public static long totalCost() {
            long totalCost = 0;
            for (long cost : taskCostMap.values()) {
                totalCost += cost;
            }
            return totalCost;
        }
    }


}
