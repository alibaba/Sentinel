package com.alibaba.csp.sentinel.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SentinelConfigTest {
    @Test
    public void test() {
        SentinelConfig sentinelConfig = new SentinelConfig();
        assertEquals(SentinelConfig.getAppName(), "sentinel-config-test");
        assertEquals(SentinelConfig.getConfig("csp.sentinel.dashboard.server"), "localhost:8080");
    }
}
