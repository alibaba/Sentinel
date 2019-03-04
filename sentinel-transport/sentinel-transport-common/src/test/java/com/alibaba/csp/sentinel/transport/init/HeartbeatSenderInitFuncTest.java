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
package com.alibaba.csp.sentinel.transport.init;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Eric Zhao
 */
public class HeartbeatSenderInitFuncTest {

    @Before
    public void setUp() throws Exception {
        SentinelConfig.removeConfig(TransportConfig.HEARTBEAT_INTERVAL_MS);
    }

    @After
    public void tearDown() throws Exception {
        SentinelConfig.removeConfig(TransportConfig.HEARTBEAT_INTERVAL_MS);
    }

    @Test
    public void testRetrieveInterval() {
        HeartbeatSender sender = mock(HeartbeatSender.class);

        long senderInterval = 5666;
        long configInterval = 6777;

        when(sender.intervalMs()).thenReturn(senderInterval);

        HeartbeatSenderInitFunc func = new HeartbeatSenderInitFunc();
        assertEquals(senderInterval, func.retrieveInterval(sender));

        // Invalid interval.
        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_INTERVAL_MS, "-1");
        assertEquals(senderInterval, func.retrieveInterval(sender));

        SentinelConfig.setConfig(TransportConfig.HEARTBEAT_INTERVAL_MS, String.valueOf(configInterval));
        assertEquals(configInterval, func.retrieveInterval(sender));
    }
}