package com.alibaba.csp.sentinel.transport.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link DashboardConfig}
 *
 * @author jz0630
 */
public class DashboardConfigTest {
    @Test
    public void testBase() {
        // no port
        DashboardConfig dashboardConfig = new DashboardConfig("10.10.10.10");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "");


        dashboardConfig = new DashboardConfig("10.10.10.10:8080");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "");
    }

    @Test
    public void testWithSchema() {
        DashboardConfig dashboardConfig = new DashboardConfig("http://10.10.10.10/");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.getSchema(), "http");


        dashboardConfig = new DashboardConfig("https://10.10.10.10:8080/");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.getSchema(), "https");

        dashboardConfig = new DashboardConfig("http://10.10.10.10");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.getSchema(), "http");


        dashboardConfig = new DashboardConfig("https://10.10.10.10:8080");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "");
        assertEquals(dashboardConfig.getSchema(), "https");
    }

    @Test
    public void testWithContextPath() {
        DashboardConfig dashboardConfig = new DashboardConfig("http://10.10.10.10/dashboard/");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "/dashboard");
        assertEquals(dashboardConfig.getSchema(), "http");

        dashboardConfig = new DashboardConfig("http://10.10.10.10:8080/dashboard/");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "/dashboard");
        assertEquals(dashboardConfig.getSchema(), "http");

        dashboardConfig = new DashboardConfig("http://10.10.10.10/dashboard");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(80));
        assertEquals(dashboardConfig.getPath(), "/dashboard");
        assertEquals(dashboardConfig.getSchema(), "http");

        dashboardConfig = new DashboardConfig("http://10.10.10.10:8080/dashboard");
        assertEquals(dashboardConfig.getHost(), "10.10.10.10");
        assertEquals(dashboardConfig.getPort(), Integer.valueOf(8080));
        assertEquals(dashboardConfig.getPath(), "/dashboard");
        assertEquals(dashboardConfig.getSchema(), "http");
    }
}
