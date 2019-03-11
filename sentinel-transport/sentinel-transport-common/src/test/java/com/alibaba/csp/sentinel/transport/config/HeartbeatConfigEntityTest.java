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
        HeartbeatConfigEntity heartbeatConfigEntity = new HeartbeatConfigEntity("10.10.10.10");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(80));
        assertEquals(heartbeatConfigEntity.getPath(), "");
        assertEquals(heartbeatConfigEntity.toString(), "HeartbeatConfigEntity{schema='http', host='10.10.10.10', port=80, path=''}");
        assertEquals(heartbeatConfigEntity.getInetSocketAddress(), new InetSocketAddress("10.10.10.10", 80));


        heartbeatConfigEntity = new HeartbeatConfigEntity("10.10.10.10:8080");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(8080));
        assertEquals(heartbeatConfigEntity.getPath(), "");
    }

    @Test
    public void testWithSchema() {
        HeartbeatConfigEntity heartbeatConfigEntity = new HeartbeatConfigEntity("http://10.10.10.10/");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(80));
        assertEquals(heartbeatConfigEntity.getPath(), "");
        assertEquals(heartbeatConfigEntity.getSchema(), "http");


        heartbeatConfigEntity = new HeartbeatConfigEntity("https://10.10.10.10:8080/");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(8080));
        assertEquals(heartbeatConfigEntity.getPath(), "");
        assertEquals(heartbeatConfigEntity.getSchema(), "https");

        heartbeatConfigEntity = new HeartbeatConfigEntity("http://10.10.10.10");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(80));
        assertEquals(heartbeatConfigEntity.getPath(), "");
        assertEquals(heartbeatConfigEntity.getSchema(), "http");


        heartbeatConfigEntity = new HeartbeatConfigEntity("https://10.10.10.10:8080");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(8080));
        assertEquals(heartbeatConfigEntity.getPath(), "");
        assertEquals(heartbeatConfigEntity.getSchema(), "https");
    }

    @Test
    public void testWithContextPath() {
        HeartbeatConfigEntity heartbeatConfigEntity = new HeartbeatConfigEntity("http://10.10.10.10/dashboard/");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(80));
        assertEquals(heartbeatConfigEntity.getPath(), "/dashboard");
        assertEquals(heartbeatConfigEntity.getSchema(), "http");

        heartbeatConfigEntity = new HeartbeatConfigEntity("http://10.10.10.10:8080/dashboard/");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(8080));
        assertEquals(heartbeatConfigEntity.getPath(), "/dashboard");
        assertEquals(heartbeatConfigEntity.getSchema(), "http");

        heartbeatConfigEntity = new HeartbeatConfigEntity("http://10.10.10.10/dashboard");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(80));
        assertEquals(heartbeatConfigEntity.getPath(), "/dashboard");
        assertEquals(heartbeatConfigEntity.getSchema(), "http");

        heartbeatConfigEntity = new HeartbeatConfigEntity("http://10.10.10.10:8080/dashboard");
        assertEquals(heartbeatConfigEntity.getHost(), "10.10.10.10");
        assertEquals(heartbeatConfigEntity.getPort(), Integer.valueOf(8080));
        assertEquals(heartbeatConfigEntity.getPath(), "/dashboard");
        assertEquals(heartbeatConfigEntity.getSchema(), "http");
    }
}
