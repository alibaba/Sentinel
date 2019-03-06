package com.alibaba.csp.sentinel.transport.config;

import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link HeartbeatConfigEntity}
 *
 * @author jz0630
 */
public class HeartbeatConfigEntityTest {
    @Test
    public void testBase() {
        // no port
        HeartbeatConfigEntity dashboardConfig = new HeartbeatConfigEntity("10.10.10.10");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.toString(), "DashboardConfig{schema='http', host='10.10.10.10', port=80, path=''}");
        assertEquals(dashboardConfig.getInetSocketAddress(), new InetSocketAddress("10.10.10.10", 80));


        dashboardConfig = new HeartbeatConfigEntity("10.10.10.10:8080");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "");
    }

    @Test
    public void testWithSchema() {
        HeartbeatConfigEntity dashboardConfig = new HeartbeatConfigEntity("http://10.10.10.10/");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.getSchema(), "http");


        dashboardConfig = new HeartbeatConfigEntity("https://10.10.10.10:8080/");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.getSchema(), "https");

        dashboardConfig = new HeartbeatConfigEntity("http://10.10.10.10");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.getSchema(), "http");


        dashboardConfig = new HeartbeatConfigEntity("https://10.10.10.10:8080");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.getSchema(), "https");
    }

    @Test
    public void testWithContextPath() {
        HeartbeatConfigEntity dashboardConfig = new HeartbeatConfigEntity("http://10.10.10.10/dashboard/");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "/dashboard");
        assertEquals(dashboardConfig.getSchema(), "http");

        dashboardConfig = new HeartbeatConfigEntity("http://10.10.10.10:8080/dashboard/");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "/dashboard");
        assertEquals(dashboardConfig.getSchema(), "http");

        dashboardConfig = new HeartbeatConfigEntity("http://10.10.10.10/dashboard");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "/dashboard");
        assertEquals(dashboardConfig.getSchema(), "http");

        dashboardConfig = new HeartbeatConfigEntity("http://10.10.10.10:8080/dashboard");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "/dashboard");
        assertEquals(dashboardConfig.getSchema(), "http");
    }
}
