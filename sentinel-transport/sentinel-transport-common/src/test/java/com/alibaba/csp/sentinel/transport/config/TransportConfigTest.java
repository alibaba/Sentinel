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
package com.alibaba.csp.sentinel.transport.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.transport.endpoint.Endpoint;
import com.alibaba.csp.sentinel.transport.endpoint.Protocol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;

public class TransportConfigTest {

    @Before
    public void setUp() throws Exception {
        SentinelConfig.removeConfig(TransportConfig.HEARTBEAT_INTERVAL_MS);
        SentinelConfig.removeConfig(TransportConfig.HEARTBEAT_CLIENT_IP);
    }

    @After
    public void tearDown() throws Exception {
        SentinelConfig.removeConfig(TransportConfig.HEARTBEAT_INTERVAL_MS);
        SentinelConfig.removeConfig(TransportConfig.HEARTBEAT_CLIENT_IP);
    }

    @Test
    public void testGetHeartbeatInterval() {
        long interval = 20000;
        assertNull(TransportConfig.getHeartbeatIntervalMs());
        // Set valid interval.
        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_INTERVAL_MS, String.valueOf(interval));
        assertEquals(new Long(interval), TransportConfig.getHeartbeatIntervalMs());
        // Set invalid interval.
        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_INTERVAL_MS, "Sentinel");
        assertNull(TransportConfig.getHeartbeatIntervalMs());
    }

    @Test
    public void testGetHeartbeatClientIp() {
        String clientIp = "10.10.10.10";
        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_CLIENT_IP, clientIp);
        // Set heartbeat client ip to system property.
        String ip = TransportConfig.getHeartbeatClientIp();

        assertNotNull(ip);
        assertEquals(clientIp, ip);

        // Set no heartbeat client ip.
        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_CLIENT_IP, "");
        assertTrue(StringUtil.isNotEmpty(TransportConfig.getHeartbeatClientIp()));
    }

    @Test
    public void testGetHeartbeatApiPath() {
        // use default heartbeat api path
        assertTrue(StringUtil.isNotEmpty(TransportConfig.getHeartbeatApiPath()));
        assertEquals(TransportConfig.HEARTBEAT_DEFAULT_PATH, TransportConfig.getHeartbeatApiPath());

        // config heartbeat api path
        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_API_PATH, "/demo");
        assertTrue(StringUtil.isNotEmpty(TransportConfig.getHeartbeatApiPath()));
        assertEquals("/demo", TransportConfig.getHeartbeatApiPath());

        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_API_PATH, "demo/registry");
        assertEquals("/demo/registry", TransportConfig.getHeartbeatApiPath());

        SentinelConfig.removeConfig(TransportConfig.HEARTBEAT_API_PATH);
        assertEquals(TransportConfig.HEARTBEAT_DEFAULT_PATH, TransportConfig.getHeartbeatApiPath());
    }
    
    @Test
    public void testGetConsoleServerList() {
        // empty
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "");
        List<Endpoint> list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(0, list.size());
        
        // single ip
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "112.13.223.3");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("112.13.223.3", list.get(0).getHost());
        assertEquals(80, list.get(0).getPort());

        // single domain
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "www.dashboard.org");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("www.dashboard.org", list.get(0).getHost());
        assertEquals(80, list.get(0).getPort());
        
        // single ip including port
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "www.dashboard.org:81");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("www.dashboard.org", list.get(0).getHost());
        assertEquals(81, list.get(0).getPort());
        
        // mixed
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "www.dashboard.org:81,112.13.223.3,112.13.223.4:8080,www.dashboard.org");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(4, list.size());
        assertEquals("www.dashboard.org", list.get(0).getHost());
        assertEquals(81, list.get(0).getPort());
        assertEquals("112.13.223.3", list.get(1).getHost());
        assertEquals(80, list.get(1).getPort());
        assertEquals("112.13.223.4", list.get(2).getHost());
        assertEquals(8080, list.get(2).getPort());
        assertEquals("www.dashboard.org", list.get(3).getHost());
        assertEquals(80, list.get(3).getPort());
        
        // malformed
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "www.dashboard.org:0");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(0, list.size());
        
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "www.dashboard.org:-1");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(0, list.size());
        
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, ":80");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(0, list.size());
        
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "www.dashboard.org:");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(0, list.size());
        
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "www.dashboard.org:80000");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(0, list.size());
        
        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "www.dashboard.org:80000,www.dashboard.org:81,:80");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("www.dashboard.org", list.get(0).getHost());
        assertEquals(81, list.get(0).getPort());

        SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "https://www.dashboard.org,http://www.dashboard.org:8080,www.dashboard.org,www.dashboard.org:8080");
        list = TransportConfig.getConsoleServerList();
        assertNotNull(list);
        assertEquals(4, list.size());
        assertEquals(Protocol.HTTPS, list.get(0).getProtocol());
        assertEquals(Protocol.HTTP, list.get(1).getProtocol());
        assertEquals(Protocol.HTTP, list.get(2).getProtocol());
        assertEquals(Protocol.HTTP, list.get(3).getProtocol());
        assertEquals(443, list.get(0).getPort());
        assertEquals(80, list.get(2).getPort());
    }
}
