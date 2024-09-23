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
package com.alibaba.csp.sentinel.event.factory;

import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.multicaster.SentinelEventMulticaster;
import com.alibaba.csp.sentinel.event.registry.SentinelEventListenerRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Daydreamer-ia
 */
public class SentinelEventMulticasterFactoryTest {

    private DefaultSentinelEventMulticasterFactory factory;
    private Properties properties;
    private SentinelEventListenerRegistry registry;
    private SentinelEventMulticaster multicaster;
    private Field defaultMulticasterField;
    private SentinelEventMulticaster defaultMulticaster;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        factory = new DefaultSentinelEventMulticasterFactory();
        properties = new Properties();
        registry = mock(SentinelEventListenerRegistry.class);
        multicaster = mock(SentinelEventMulticaster.class);
        factory.init(properties, registry);

        Field dafaultField = DefaultSentinelEventMulticasterFactory.class.getDeclaredField("globalSentinelEventMulticaster");
        dafaultField.setAccessible(true);
        defaultMulticasterField = dafaultField;
        defaultMulticaster = (SentinelEventMulticaster) defaultMulticasterField.get(factory);
    }

    @After
    public void tearDown() {
        factory.destroy();
    }

    @Test
    public void testAddSentinelEventMulticaster() {
        Class<? extends SentinelEvent> eventClass = TestSentinelEvent.class;
        boolean result = factory.addSentinelEventMulticaster(eventClass, multicaster);
        assertTrue(result);
        SentinelEventMulticaster sentinelEventMulticaster = factory.getSentinelEventMulticaster(eventClass);
        assertEquals(multicaster, sentinelEventMulticaster);
    }

    @Test
    public void testRemoveSentinelEventMulticaster() {
        Class<? extends SentinelEvent> eventClass = TestSentinelEvent.class;
        factory.addSentinelEventMulticaster(eventClass, multicaster);
        boolean result = factory.removeSentinelEventMulticaster(eventClass);
        assertTrue(result);
        assertEquals(factory.getSentinelEventMulticaster(eventClass), defaultMulticaster);
    }

    @Test
    public void testGetSentinelEventMulticaster_ReturnsGlobalWhenNotFound() {
        Class<? extends SentinelEvent> eventClass = TestSentinelEvent.class;
        SentinelEventMulticaster result = factory.getSentinelEventMulticaster(eventClass);
        // return default if not setting
        assertEquals(defaultMulticaster, result);
    }

    /**
     * for test only
     */
    private static class TestSentinelEvent extends SentinelEvent {
    }
}
