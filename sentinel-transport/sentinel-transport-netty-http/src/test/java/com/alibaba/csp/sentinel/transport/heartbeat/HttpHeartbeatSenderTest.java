package com.alibaba.csp.sentinel.transport.heartbeat;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.function.Tuple2;

public class HttpHeartbeatSenderTest {
    
    private void setAddr(String serverList) {
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, serverList);
    }
    
    @Test
    public void testAddr() {
        setAddr("");
        assertEquals(0, HttpHeartbeatSender.parseDashboardList().size());
        
        setAddr("a.com");
        List<Tuple2<String, Integer>> list = HttpHeartbeatSender.parseDashboardList();
        assertEquals(1, list.size());
        assertEquals("a.com", list.get(0).r1);
        assertEquals(Integer.valueOf(80), list.get(0).r2);
        
        setAddr("a.com:88");
        list = HttpHeartbeatSender.parseDashboardList();
        assertEquals(1, list.size());
        assertEquals("a.com", list.get(0).r1);
        assertEquals(Integer.valueOf(88), list.get(0).r2);
        
        setAddr("a.com:88,,,,");
        list = HttpHeartbeatSender.parseDashboardList();
        assertEquals(1, list.size());
        assertEquals("a.com", list.get(0).r1);
        assertEquals(Integer.valueOf(88), list.get(0).r2);
        
        setAddr("a.com:88,b.com");
        list = HttpHeartbeatSender.parseDashboardList();
        assertEquals(2, list.size());
        assertEquals("a.com", list.get(0).r1);
        assertEquals(Integer.valueOf(88), list.get(0).r2);
        assertEquals("b.com", list.get(1).r1);
        assertEquals(Integer.valueOf(80), list.get(1).r2);
        
        setAddr("a.com:88,b.com:99999");
        list = HttpHeartbeatSender.parseDashboardList();
        assertEquals(2, list.size());
        assertEquals("a.com", list.get(0).r1);
        assertEquals(Integer.valueOf(88), list.get(0).r2);
        assertEquals("b.com", list.get(1).r1);
        assertEquals(Integer.valueOf(99999), list.get(1).r2);
    }
}
