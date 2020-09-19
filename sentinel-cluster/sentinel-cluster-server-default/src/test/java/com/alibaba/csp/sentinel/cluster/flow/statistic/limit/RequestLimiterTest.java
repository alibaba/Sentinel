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
package com.alibaba.csp.sentinel.cluster.flow.statistic.limit;

import com.alibaba.csp.sentinel.cluster.test.AbstractTimeBasedTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestLimiterTest extends AbstractTimeBasedTest {

    @Test
    public void testRequestLimiter() {
        setCurrentMillis(System.currentTimeMillis());
        RequestLimiter limiter = new RequestLimiter(10);
        limiter.add(3);
        limiter.add(3);
        limiter.add(3);
        assertTrue(limiter.canPass());
        assertEquals(9, limiter.getSum());
        limiter.add(3);
        assertFalse(limiter.canPass());

        // wait a second to refresh the window
        sleep(1000);
        limiter.add(3);
        assertTrue(limiter.tryPass());
        assertTrue(limiter.canPass());
        assertEquals(4, limiter.getSum());
    }
}
