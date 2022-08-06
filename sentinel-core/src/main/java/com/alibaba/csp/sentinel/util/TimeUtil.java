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
package com.alibaba.csp.sentinel.util;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import com.alibaba.csp.sentinel.util.function.Tuple2;

/**
 * <p>Provides millisecond-level time of OS.</p>
 * <p>
 * Here we should see that not all the time TimeUtil should
 * keep looping 1_000 times every second (Actually about 800/s due to some losses).
 * <pre>
 * * In idle conditions it just acts as System.currentTimeMillis();
 * * In busy conditions (significantly more than 1_000/s) it keeps loop to reduce costs.
 * </pre>
 * For detail design and proposals please goto
 * <a href="https://github.com/alibaba/Sentinel/issues/1702#issuecomment-692151160">https://github.com/alibaba/Sentinel/issues/1702</a>
 *
 * @author qinan.qn
 * @author jason
 */
public final class TimeUtil implements Runnable {
    private static final long CHECK_INTERVAL = 3000;
    private static final long HITS_LOWER_BOUNDARY = 800;
    private static final long HITS_UPPER_BOUNDARY = 1200;

    public static enum STATE {
        IDLE,
        PREPARE,
        RUNNING;
    }

    private static class Statistic {
        private final LongAdder writes = new LongAdder();
        private final LongAdder reads = new LongAdder();

        public LongAdder getWrites() {
            return writes;
        }

        public LongAdder getReads() {
            return reads;
        }
    }

    private static TimeUtil INSTANCE;

    private volatile long currentTimeMillis;
    private volatile STATE state = STATE.IDLE;

    private LeapArray<Statistic> statistics;

    /**
     * thread private variables
     */
    private long lastCheck = 0;

    static {
        INSTANCE = new TimeUtil();
    }

    public TimeUtil() {
        this.statistics = new LeapArray<TimeUtil.Statistic>(3, 3000) {

            @Override
            public Statistic newEmptyBucket(long timeMillis) {
                return new Statistic();
            }

            @Override
            protected WindowWrap<Statistic> resetWindowTo(WindowWrap<Statistic> windowWrap, long startTime) {
                Statistic val = windowWrap.value();
                val.getReads().reset();
                val.getWrites().reset();
                windowWrap.resetTo(startTime);
                return windowWrap;
            }
        };
        this.currentTimeMillis = System.currentTimeMillis();
        this.lastCheck = this.currentTimeMillis;
        Thread daemon = new Thread(this);
        daemon.setDaemon(true);
        daemon.setName("sentinel-time-tick-thread");
        daemon.start();
    }

    @Override
    public void run() {
        while (true) {
            // Mechanism optimized since 1.8.2
            this.check();
            if (this.state == STATE.RUNNING) {
                this.currentTimeMillis = System.currentTimeMillis();
                this.statistics.currentWindow(this.currentTimeMillis).value().getWrites().increment();
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (Throwable e) {
                }
                continue;
            }
            if (this.state == STATE.IDLE) {
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (Throwable e) {
                }
                continue;
            }
            if (this.state == STATE.PREPARE) {
                RecordLog.debug("TimeUtil switches to RUNNING");
                this.currentTimeMillis = System.currentTimeMillis();
                this.state = STATE.RUNNING;
                continue;
            }
        }
    }

    /**
     * Current running state
     *
     * @return
     */
    public STATE getState() {
        return state;
    }

    /**
     * Current qps statistics (including reads and writes request)
     * excluding current working time window for accurate result.
     *
     * @param now
     * @return
     */
    public Tuple2<Long, Long> currentQps(long now) {
        List<WindowWrap<Statistic>> list = this.statistics.listAll();
        long reads = 0;
        long writes = 0;
        int cnt = 0;
        for (WindowWrap<Statistic> windowWrap : list) {
            if (windowWrap.isTimeInWindow(now)) {
                continue;
            }
            cnt++;
            reads += windowWrap.value().getReads().longValue();
            writes += windowWrap.value().getWrites().longValue();
        }
        if (cnt < 1) {
            return new Tuple2<Long, Long>(0L, 0L);
        }
        return new Tuple2<Long, Long>(reads / cnt, writes / cnt);
    }

    /**
     * Check and operate the state if necessary.
     * ATTENTION: It's called in daemon thread.
     */
    private void check() {
        long now = currentTime(true);
        // every period
        if (now - this.lastCheck < CHECK_INTERVAL) {
            return;
        }
        this.lastCheck = now;
        Tuple2<Long, Long> qps = currentQps(now);
        if (this.state == STATE.IDLE && qps.r1 > HITS_UPPER_BOUNDARY) {
            RecordLog.info("TimeUtil switches to PREPARE for better performance, reads={}/s, writes={}/s", qps.r1, qps.r2);
            this.state = STATE.PREPARE;
        } else if (this.state == STATE.RUNNING && qps.r1 < HITS_LOWER_BOUNDARY) {
            RecordLog.info("TimeUtil switches to IDLE due to not enough load, reads={}/s, writes={}/s", qps.r1, qps.r2);
            this.state = STATE.IDLE;
        }
    }

    private long currentTime(boolean innerCall) {
        long now = this.currentTimeMillis;
        Statistic val = this.statistics.currentWindow(now).value();
        if (!innerCall) {
            val.getReads().increment();
        }
        if (this.state == STATE.IDLE || this.state == STATE.PREPARE) {
            now = System.currentTimeMillis();
            this.currentTimeMillis = now;
            if (!innerCall) {
                val.getWrites().increment();
            }
        }
        return now;
    }

    /**
     * Current timestamp in milliseconds.
     *
     * @return
     */
    public long getTime() {
        return this.currentTime(false);
    }

    public static TimeUtil instance() {
        return INSTANCE;
    }

    public static long currentTimeMillis() {
        return INSTANCE.getTime();
    }
}
