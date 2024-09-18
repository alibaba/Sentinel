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
import com.alibaba.csp.sentinel.event.model.ParamFlowBlockEvent;
import com.alibaba.csp.sentinel.event.model.SentinelEvent;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * @author Daydreamer-ia
 */
public class EventSlotParamFlowTest {

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
    public void testParamFlowBlock() throws Throwable {
        ParamFlowRule paramFlowRule = new ParamFlowRule("testApp");
        doThrow(new ParamFlowException("testApp", "test", paramFlowRule)).when(eventSlot).fireEntry(any(), any(), any(), anyInt(), anyBoolean(), any());

        class TestParamFlowListener extends SentinelEventListener {

            @Override
            public void onEvent(SentinelEvent event) {
                assertEquals(event.getClass(), ParamFlowBlockEvent.class);
                assertEquals(((ParamFlowBlockEvent) event).getResourceName(), "testApp");
                b.set(true);
            }

            @Override
            public List<Class<? extends SentinelEvent>> eventType() {
                return Collections.singletonList(ParamFlowBlockEvent.class);
            }
        }
        SentinelEventBus.getInstance().addListener(new TestParamFlowListener());
        try {
            eventSlot.entry(context, resourceWrapper, "", 1, true, "");
        } catch (Exception e) {
            assertEquals(e.getClass(), ParamFlowException.class);
        }
        Thread.sleep(200);
        assertTrue(b.get());
    }
}
