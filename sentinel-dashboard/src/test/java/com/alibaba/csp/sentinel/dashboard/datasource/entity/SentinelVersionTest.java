package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import static org.junit.Assert.*;

import org.junit.Test;

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
