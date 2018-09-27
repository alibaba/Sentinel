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
package com.alibaba.csp.sentinel.slots.statistic.data;

import com.alibaba.csp.sentinel.slots.block.flow.param.RollingParamEvent;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link ParamMapBucket}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamMapBucketTest {

    @Test
    public void testAddEviction() {
        ParamMapBucket bucket = new ParamMapBucket();
        for (int i = 0; i < ParamMapBucket.DEFAULT_MAX_CAPACITY; i++) {
            bucket.add(RollingParamEvent.REQUEST_PASSED, 1, "param-" + i);
        }
        String lastParam = "param-end";
        bucket.add(RollingParamEvent.REQUEST_PASSED, 1, lastParam);
        assertEquals(0, bucket.get(RollingParamEvent.REQUEST_PASSED, "param-0"));
        assertEquals(1, bucket.get(RollingParamEvent.REQUEST_PASSED, "param-1"));
        assertEquals(1, bucket.get(RollingParamEvent.REQUEST_PASSED, lastParam));
    }

    @Test
    public void testAddGetResetCommon() {
        ParamMapBucket bucket = new ParamMapBucket();
        double paramA = 1.1d;
        double paramB = 2.2d;
        double paramC = -19.7d;
        // Block: A 5 | B 1 | C 6
        // Pass: A 0 | B 1 | C 7
        bucket.add(RollingParamEvent.REQUEST_BLOCKED, 3, paramA);
        bucket.add(RollingParamEvent.REQUEST_PASSED, 1, paramB);
        bucket.add(RollingParamEvent.REQUEST_BLOCKED, 1, paramB);
        bucket.add(RollingParamEvent.REQUEST_BLOCKED, 2, paramA);
        bucket.add(RollingParamEvent.REQUEST_PASSED, 6, paramC);
        bucket.add(RollingParamEvent.REQUEST_BLOCKED, 4, paramC);
        bucket.add(RollingParamEvent.REQUEST_PASSED, 1, paramC);
        bucket.add(RollingParamEvent.REQUEST_BLOCKED, 2, paramC);

        assertEquals(5, bucket.get(RollingParamEvent.REQUEST_BLOCKED, paramA));
        assertEquals(1, bucket.get(RollingParamEvent.REQUEST_BLOCKED, paramB));
        assertEquals(6, bucket.get(RollingParamEvent.REQUEST_BLOCKED, paramC));
        assertEquals(0, bucket.get(RollingParamEvent.REQUEST_PASSED, paramA));
        assertEquals(1, bucket.get(RollingParamEvent.REQUEST_PASSED, paramB));
        assertEquals(7, bucket.get(RollingParamEvent.REQUEST_PASSED, paramC));

        bucket.reset();
        assertEquals(0, bucket.get(RollingParamEvent.REQUEST_BLOCKED, paramA));
        assertEquals(0, bucket.get(RollingParamEvent.REQUEST_BLOCKED, paramB));
        assertEquals(0, bucket.get(RollingParamEvent.REQUEST_BLOCKED, paramC));
        assertEquals(0, bucket.get(RollingParamEvent.REQUEST_PASSED, paramA));
        assertEquals(0, bucket.get(RollingParamEvent.REQUEST_PASSED, paramB));
        assertEquals(0, bucket.get(RollingParamEvent.REQUEST_PASSED, paramC));
    }
}