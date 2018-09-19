package com.alibaba.csp.sentinel.slots.statistic.data;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.hotspot.RollingParamEvent;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class HotParamMapBucketTest {

    @Test
    public void testAddEviction() {
        HotParamMapBucket bucket = new HotParamMapBucket();
        for (int i = 0; i < HotParamMapBucket.DEFAULT_MAX_CAPACITY; i++) {
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
        HotParamMapBucket bucket = new HotParamMapBucket();
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