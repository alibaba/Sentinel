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
package com.alibaba.csp.sentinel.demo.flow.param;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * A traffic runner to simulate flow for different parameters.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
class ParamFlowQpsRunner<T> {

    private final T[] params;
    private final String resourceName;
    private int seconds;
    private final int threadCount;

    private final Map<T, AtomicLong> passCountMap = new ConcurrentHashMap<>();
    private final Map<T, AtomicLong> blockCountMap = new ConcurrentHashMap<>();

    private volatile boolean stop = false;

    public ParamFlowQpsRunner(T[] params, String resourceName, int threadCount, int seconds) {
        assertTrue(params != null && params.length > 0, "Parameter array should not be empty");
        assertTrue(StringUtil.isNotBlank(resourceName), "Resource name cannot be empty");
        assertTrue(seconds > 0, "Time period should be positive");
        assertTrue(threadCount > 0 && threadCount <= 1000, "Invalid thread count");
        this.params = params;
        this.resourceName = resourceName;
        this.seconds = seconds;
        this.threadCount = threadCount;

        for (T param : params) {
            assertTrue(param != null, "Parameters should not be null");
            passCountMap.putIfAbsent(param, new AtomicLong());
            blockCountMap.putIfAbsent(param, new AtomicLong());
        }
    }

    private void assertTrue(boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Pick one of provided parameters randomly.
     *
     * @return picked parameter
     */
    private T generateParam() {
        int i = ThreadLocalRandom.current().nextInt(0, params.length);
        return params[i];
    }

    void simulateTraffic() {
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new RunTask());
            t.setName("sentinel-simulate-traffic-task-" + i);
            t.start();
        }
    }

    void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    private void passFor(T param) {
        passCountMap.get(param).incrementAndGet();
        // System.out.println(String.format("Parameter <%s> passed at: %d", param, TimeUtil.currentTimeMillis()));
    }

    private void blockFor(T param) {
        blockCountMap.get(param).incrementAndGet();
    }

    final class RunTask implements Runnable {

        @Override
        public void run() {

            while (!stop) {
                Entry entry = null;
                T param = generateParam();
                try {
                    entry = SphU.entry(resourceName, EntryType.IN, 1, param);
                    // Add pass for parameter.
                    passFor(param);
                } catch (BlockException e) {
                    // block.incrementAndGet();
                    blockFor(param);
                } catch (Exception ex) {
                    // biz exception
                    ex.printStackTrace();
                } finally {
                    // total.incrementAndGet();
                    if (entry != null) {
                        entry.exit(1, param);
                    }
                }

                sleep(ThreadLocalRandom.current().nextInt(0, 10));
            }
        }
    }

    private void sleep(int timeMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeMs);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    final class TimerTask implements Runnable {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("Begin to run! Go go go!");
            System.out.println("See corresponding metrics.log for accurate statistic data");

            Map<T, Long> map = new HashMap<>(params.length);
            for (T param : params) {
                map.putIfAbsent(param, 0L);
            }
            while (!stop) {
                sleep(1000);

                // There may be a mismatch for time window of internal sliding window.
                // See corresponding `metrics.log` for accurate statistic log.
                for (T param : params) {

                    System.out.println(String.format(
                        "[%d][%d] Parameter flow metrics for resource %s: "
                            + "pass count for param <%s> is %d, block count: %d",
                        seconds, TimeUtil.currentTimeMillis(), resourceName, param,
                        passCountMap.get(param).getAndSet(0), blockCountMap.get(param).getAndSet(0)));
                }
                System.out.println("=============================");
                if (seconds-- <= 0) {
                    stop = true;
                }
            }

            long cost = System.currentTimeMillis() - start;
            System.out.println("Time cost: " + cost + " ms");
            System.exit(0);
        }
    }
}
