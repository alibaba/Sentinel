/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.function.Tuple2;

/**
 * @author jason
 *
 */
public class TimeUtilTest {
    @Before
    public void initLogging() {
        System.setProperty("csp.sentinel.log.output.type", "console");
    }
    
    private void waitFor(int step, int seconds) throws InterruptedException {
        for (int i = 0; i < seconds; i ++) {
            Tuple2<Long, Long> qps = TimeUtil.instance().currentQps(TimeUtil.currentTimeMillis());
            RecordLog.info("step {} qps: reads={}, writes={}", step, qps.r1, qps.r2);
            Thread.sleep(1000);
        }
    }
    
    @Test
    public void test() throws InterruptedException {
        final AtomicInteger delayTime = new AtomicInteger(50);
        final AtomicBoolean shouldShutdown = new AtomicBoolean();
        for (int i = 0; i < 10; i ++) {
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    long last = 0;
                    while (true) {
                        if (shouldShutdown.get()) {
                            break;
                        }
                        long now = TimeUtil.currentTimeMillis();
                        int delay = delayTime.get();
                        if (delay < 1) {
                            if (last > now) {
                                System.err.println("wrong value");
                            }
                            last = now;
                            continue;
                        }
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                        }
                        if (last > now) {
                            System.err.println("incorrect value");
                        }
                        last = now;
                    }
                }
            }).start();
        }
        Tuple2<Long, Long> qps;
        waitFor(1, 4);
        // initial state
        assertEquals(TimeUtil.STATE.IDLE, TimeUtil.instance().getState());
        qps = TimeUtil.instance().currentQps(TimeUtil.currentTimeMillis());
        assertTrue(qps.r1 < 1000);
        assertTrue(qps.r2 < 1000);
        
        // to RUNNING
        delayTime.set(0);
        // wait statistics to be stable
        waitFor(2, 8);
        qps = TimeUtil.instance().currentQps(TimeUtil.currentTimeMillis());
        assertEquals(TimeUtil.STATE.RUNNING, TimeUtil.instance().getState());
        assertTrue(qps.r1 > 1000);
        assertTrue(qps.r2 <= 1000);
        
        // back
        delayTime.set(100);
        // wait statistics to be stable
        waitFor(3, 8);
        qps = TimeUtil.instance().currentQps(TimeUtil.currentTimeMillis());
        assertEquals(TimeUtil.STATE.IDLE, TimeUtil.instance().getState());
        assertTrue(qps.r1 < 1000);
        assertTrue(qps.r2 < 1000);
        shouldShutdown.set(true);
    }
    
}
