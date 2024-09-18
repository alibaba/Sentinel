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
package com.alibaba.csp.sentinel.slots.event;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.event.SentinelEventBus;
import com.alibaba.csp.sentinel.event.SentinelEventListener;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.AuthorityBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.FlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.impl.block.SystemBlockEvent;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Daydreamer-ia
 */
public class EventSlotTest {

    private EventSlot eventSlot;

    private AtomicBoolean b;

    private Context context;

    private ResourceWrapper resourceWrapper;

    @Before
    public void setUp() throws Exception {
        eventSlot = Mockito.spy(EventSlot.class);
        b = new AtomicBoolean(false);
        resourceWrapper = new StringResourceWrapper("testApp", EntryType.IN);
        context = new Context(new DefaultNode(resourceWrapper, new ClusterNode("testApp")), "testApp");
    }

    @Test
    public void testAuthorityBlock() throws Throwable {
        AuthorityRule authorityRule = new AuthorityRule();
        doThrow(new AuthorityException("testApp", authorityRule)).when(eventSlot).fireEntry(any(), any(), any(), anyInt(), anyBoolean(), any());

        class TestAuthorityListener extends SentinelEventListener {

            @Override
            public void onEvent(SentinelEvent event) {
                assertEquals(event.getClass(), AuthorityBlockEvent.class);
                b.set(true);
            }

            @Override
            public List<Class<? extends SentinelEvent>> eventType() {
                return Collections.singletonList(AuthorityBlockEvent.class);
            }
        }
        SentinelEventBus.getInstance().addListener(new TestAuthorityListener());
        try {
            eventSlot.entry(context, resourceWrapper, "", 1, true, "");
        } catch (Exception e) {
            assertEquals(e.getClass(), AuthorityException.class);
        }
        Thread.sleep(200);
        assertTrue(b.get());
    }

    @Test
    public void testFlowBlock() throws Throwable {
        FlowRule flowRule = new FlowRule();
        doThrow(new FlowException("testApp", flowRule)).when(eventSlot).fireEntry(any(), any(), any(), anyInt(), anyBoolean(), any());

        class TestFlowListener extends SentinelEventListener {

            @Override
            public void onEvent(SentinelEvent event) {
                assertEquals(event.getClass(), FlowBlockEvent.class);
                b.set(true);
            }

            @Override
            public List<Class<? extends SentinelEvent>> eventType() {
                return Collections.singletonList(FlowBlockEvent.class);
            }
        }
        SentinelEventBus.getInstance().addListener(new TestFlowListener());
        try {
            eventSlot.entry(context, resourceWrapper, "", 1, true, "");
        } catch (Exception e) {
            assertEquals(e.getClass(), FlowException.class);
        }
        Thread.sleep(200);
        assertTrue(b.get());
    }

    @Test
    public void testSysBlock() throws Throwable {
        doThrow(new SystemBlockException("testApp", "rt")).when(eventSlot).fireEntry(any(), any(), any(), anyInt(), anyBoolean(), any());

        class TestSysListener extends SentinelEventListener {

            @Override
            public void onEvent(SentinelEvent event) {
                assertEquals(event.getClass(), SystemBlockEvent.class);
                assertEquals(((SystemBlockEvent) event).getSysMetricKey(), "rt");
                b.set(true);
            }

            @Override
            public List<Class<? extends SentinelEvent>> eventType() {
                return Collections.singletonList(SystemBlockEvent.class);
            }
        }
        SentinelEventBus.getInstance().addListener(new TestSysListener());
        try {
            eventSlot.entry(context, resourceWrapper, "", 1, true, "");
        } catch (Exception e) {
            assertEquals(e.getClass(), SystemBlockException.class);
        }
        Thread.sleep(200);
        assertTrue(b.get());
    }
}
