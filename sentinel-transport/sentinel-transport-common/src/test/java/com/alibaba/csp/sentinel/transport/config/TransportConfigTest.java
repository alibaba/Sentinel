package com.alibaba.csp.sentinel.transport.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransportConfigTest {

    @Test
    public void getClientIp() {
        //config heartbeat client ip
        System.setProperty(TransportConfig.HEARTBEAT_CLIENT_IP, "10.10.10.10");
        String ip = TransportConfig.getHeartbeatClientIp();

        assertNotNull(ip);
        assertEquals(ip, "10.10.10.10");

        //no heartbeat client ip
        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_CLIENT_IP, "");
        ip = TransportConfig.getHeartbeatClientIp();
        assertNotNull(ip);

    }
}