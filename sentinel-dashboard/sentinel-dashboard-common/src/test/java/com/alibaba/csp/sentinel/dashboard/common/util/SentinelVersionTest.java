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
package com.alibaba.csp.sentinel.dashboard.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SentinelVersionTest {

    @Test
    public void testEqual() {
        assertEquals(new SentinelVersion(1, 0, 0), new SentinelVersion(1, 0, 0));
        assertNotEquals(new SentinelVersion(1, 0, 0), new SentinelVersion(1, 2, 3));
        assertNotEquals(new SentinelVersion(1, 0, 0), new SentinelVersion(1, 0, 0, ""));
        assertEquals(new SentinelVersion(1, 0, 0, ""), new SentinelVersion(1, 0, 0, ""));
        assertNotEquals(new SentinelVersion(1, 0, 0, ""), new SentinelVersion(1, 0, 0, null));
        assertEquals(new SentinelVersion(1, 0, 0, null), new SentinelVersion(1, 0, 0, null));
    }

    @Test
    public void testGreater() {
        assertTrue(new SentinelVersion(2, 0, 0).greaterThan(new SentinelVersion(1, 0, 0)));
        assertTrue(new SentinelVersion(1, 1, 0).greaterThan(new SentinelVersion(1, 0, 0)));
        assertTrue(new SentinelVersion(1, 1, 2).greaterThan(new SentinelVersion(1, 1, 0)));
        assertTrue(new SentinelVersion(1, 1, 4).greaterThan(new SentinelVersion(1, 1, 3)));
        assertFalse(new SentinelVersion(1, 0, 0).greaterThan(new SentinelVersion(1, 0, 0)));
        assertFalse(new SentinelVersion(1, 0, 0).greaterThan(new SentinelVersion(1, 1, 0)));
        assertFalse(new SentinelVersion(1, 1, 3).greaterThan(new SentinelVersion(1, 1, 3)));
        assertFalse(new SentinelVersion(1, 1, 2).greaterThan(new SentinelVersion(1, 1, 3)));
        assertFalse(new SentinelVersion(1, 0, 0, "").greaterThan(new SentinelVersion(1, 0, 0)));
        assertTrue(new SentinelVersion(1, 0, 1).greaterThan(new SentinelVersion(1, 0, 0)));
        assertTrue(new SentinelVersion(1, 0, 1, "a").greaterThan(new SentinelVersion(1, 0, 0, "b")));
        assertFalse(new SentinelVersion(1, 0, 0, "b").greaterThan(new SentinelVersion(1, 0, 0, "a")));
    }
}
