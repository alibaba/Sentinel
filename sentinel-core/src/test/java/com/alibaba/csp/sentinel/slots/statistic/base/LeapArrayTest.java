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
package com.alibaba.csp.sentinel.slots.statistic.base;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class LeapArrayTest extends AbstractTimeBasedTest {
    
    @Test
    public void testGetValidHead() {
        int windowLengthInMs = 100;
        int intervalInMs = 1000;
        int sampleCount = intervalInMs / windowLengthInMs;
        LeapArray<AtomicInteger> leapArray = new LeapArray<AtomicInteger>(sampleCount, intervalInMs) {
            @Override
            public AtomicInteger newEmptyBucket(long time) {
                return new AtomicInteger(0);
            }

            @Override
            protected WindowWrap<AtomicInteger> resetWindowTo(WindowWrap<AtomicInteger> windowWrap, long startTime) {
                windowWrap.resetTo(startTime);
                windowWrap.value().set(0);
                return windowWrap;
            }
        };
        
        WindowWrap<AtomicInteger> expected1 = leapArray.currentWindow();
        expected1.value().addAndGet(1);
        sleep(windowLengthInMs);
        WindowWrap<AtomicInteger> expected2 = leapArray.currentWindow();
        expected2.value().addAndGet(2);
        for (int i = 0; i < sampleCount - 2; i++) {
            sleep(windowLengthInMs);
            leapArray.currentWindow().value().addAndGet(i + 3);
        }

        assertSame(expected1, leapArray.getValidHead());
        sleep(windowLengthInMs);
        assertSame(expected2, leapArray.getValidHead());
    }

}