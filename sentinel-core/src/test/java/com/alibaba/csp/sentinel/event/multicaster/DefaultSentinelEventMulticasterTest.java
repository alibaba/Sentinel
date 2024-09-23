/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.event.multicaster;

import com.alibaba.csp.sentinel.event.EventProperties;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.registry.SentinelEventListenerRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Daydreamer-ia
 */
public class DefaultSentinelEventMulticasterTest {

    private DefaultSentinelEventMulticaster multicaster;
    private Properties properties;
    private SentinelEventListenerRegistry registry;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        properties = new Properties();
        properties.setProperty(EventProperties.MAX_EVENT_QUEUE_SIZE, "10");

        registry = mock(SentinelEventListenerRegistry.class);
        multicaster = new DefaultSentinelEventMulticaster();
    }

    @After
    public void tearDown() {
        if (multicaster != null) {
            multicaster.destroy();
        }
    }

    @Test
    public void testInit() {
        multicaster.init(properties, registry);
        assertNotNull(multicaster.queue);
        assertTrue(multicaster.isRunning());
    }

    @Test
    public void testPublish_Success() {
        SentinelEvent event = mock(SentinelEvent.class);
        multicaster.init(properties, registry);
        multicaster.destroy();
        boolean result = multicaster.publish(event);
        assertTrue(result);
        assertEquals(event, multicaster.queue.poll());
    }

    @Test
    public void testPublish_Failed() {
        multicaster.init(properties, registry);
        multicaster.destroy();
        // Mocking a full queue situation
        SentinelEvent event = mock(SentinelEvent.class);
        // Fill the queue to make it full
        for (int i = 0; i < 10; i++) {
            multicaster.queue.offer(mock(SentinelEvent.class));
        }
        boolean result = multicaster.publish(event);
        assertTrue(result);
        assertEquals(10, multicaster.queue.size());
    }

    @Test
    public void testRun() throws InterruptedException {
        SentinelEvent event = mock(SentinelEvent.class);
        multicaster.init(properties, registry);
        multicaster.publish(event);
        // Sleep a moment to allow the run method to process the event
        Thread.sleep(150);
        // Verify that receiveEvent has been called
        verify(registry, atLeastOnce()).getSentinelEventListener(any());

        multicaster.destroy();
        assertFalse(multicaster.isRunning());
    }

    @Test
    public void testDestroy() {
        multicaster.init(properties, registry);
        multicaster.destroy();
        assertFalse(multicaster.isRunning());
        assertTrue(multicaster.queue.isEmpty());
    }
}

