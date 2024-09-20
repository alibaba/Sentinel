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
package com.alibaba.csp.sentinel.slots.statistic;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.PriorityWaitException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author cdfive
 */
public class StatisticSlotTest {

    @Before
    public void setUp() {
        ContextTestUtil.cleanUpContext();
    }

    @After
    public void cleanUp() {
        ContextTestUtil.cleanUpContext();
    }

    @Test
    public void testFireEntry() throws Throwable {
        StatisticSlot slot = mock(StatisticSlot.class);

        Context context = ContextUtil.enter("serviceA");
        ResourceWrapper resourceWrapper = new StringResourceWrapper("nodeA", EntryType.IN);
        DefaultNode node = new DefaultNode(resourceWrapper, new ClusterNode());

        Entry entry = mock(Entry.class);
        context.setCurEntry(entry);
        when(entry.getCurNode()).thenReturn(node);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        slot.entry(context, resourceWrapper, node, 1, false);

        verify(slot).entry(context, resourceWrapper, node, 1, false);
        // Verify fireEntry method has been called, and only once
        verify(slot).fireEntry(context, resourceWrapper, node, 1, false);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testFireExit() throws Throwable {
        StatisticSlot slot = mock(StatisticSlot.class);
        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        DefaultNode node = mock(DefaultNode.class);
        when(context.getCurNode()).thenReturn(node);
        Entry entry = mock(Entry.class);
        when(context.getCurEntry()).thenReturn(entry);

        doCallRealMethod().when(slot).exit(context, resourceWrapper, 1);
        slot.exit(context, resourceWrapper, 1);

        verify(slot).exit(context, resourceWrapper, 1);
        // Verify fireExit method has been called, and only once
        verify(slot).fireExit(context, resourceWrapper, 1);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testEntry() throws Throwable {
        StatisticSlot slot = mock(StatisticSlot.class);
        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        when(resourceWrapper.getType()).thenReturn(EntryType.IN);
        DefaultNode node = mock(DefaultNode.class);
        Entry entry = mock(Entry.class);
        when(context.getCurEntry()).thenReturn(entry);
        DefaultNode originNode = mock(DefaultNode.class);
        when(entry.getOriginNode()).thenReturn(originNode);
        ClusterNode clusterNode = mock(ClusterNode.class);
        Whitebox.setInternalState(Constants.class, "ENTRY_NODE", clusterNode);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        slot.entry(context, resourceWrapper, node, 1, false);

        // To verify call fireEntry firstly
        InOrder inOrder = inOrder(slot, node, originNode, clusterNode);

        inOrder.verify(slot).entry(context, resourceWrapper, node, 1, false);

        // Verify call fireEntry firstly
        inOrder.verify(slot).fireEntry(context, resourceWrapper, node, 1, false);
        verifyNoMoreInteractions(slot);

        // Verify node,originNode,Constants.ENTRY_NODE counted threadNum and passed_pqs, and not counted anything else
        inOrder.verify(node).increaseThreadNum();
        inOrder.verify(node).addPassRequest(1);
        verifyNoMoreInteractions(node);

        inOrder.verify(originNode).increaseThreadNum();
        inOrder.verify(originNode).addPassRequest(1);
        verifyNoMoreInteractions(originNode);

        inOrder.verify(clusterNode).increaseThreadNum();
        inOrder.verify(clusterNode).addPassRequest(1);
        verifyNoMoreInteractions(clusterNode);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testEntryBlockException() throws Throwable {
        StatisticSlot slot = mock(StatisticSlot.class);
        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        when(resourceWrapper.getType()).thenReturn(EntryType.IN);
        DefaultNode node = mock(DefaultNode.class);
        Entry entry = mock(Entry.class);
        when(context.getCurEntry()).thenReturn(entry);
        DefaultNode originNode = mock(DefaultNode.class);
        when(entry.getOriginNode()).thenReturn(originNode);
        ClusterNode clusterNode = mock(ClusterNode.class);
        Whitebox.setInternalState(Constants.class, "ENTRY_NODE", clusterNode);

        // Mock throw a BlockException, e.g. FlowException
        doThrow(new FlowException("test")).when(slot).fireEntry(context, resourceWrapper, node, 1, false);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        try {
            slot.entry(context, resourceWrapper, node, 1, false);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof BlockException);

            // Verify node,originNode,Constants.ENTRY_NODE counted block_qps, and not counted threadNum or anything else
            verify(node).increaseBlockQps(1);
            verify(node, never()).increaseThreadNum();
            verifyNoMoreInteractions(node);

            verify(originNode).increaseBlockQps(1);
            verify(originNode, never()).increaseThreadNum();
            verifyNoMoreInteractions(originNode);

            verify(clusterNode).increaseBlockQps(1);
            verify(clusterNode, never()).increaseThreadNum();
            verifyNoMoreInteractions(clusterNode);
        }
    }

    @Test
    public void testEntryPriorityWaitException() throws Throwable {
        StatisticSlot slot = mock(StatisticSlot.class);
        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        when(resourceWrapper.getType()).thenReturn(EntryType.IN);
        DefaultNode node = mock(DefaultNode.class);
        Entry entry = mock(Entry.class);
        when(context.getCurEntry()).thenReturn(entry);
        DefaultNode originNode = mock(DefaultNode.class);
        when(entry.getOriginNode()).thenReturn(originNode);
        ClusterNode clusterNode = mock(ClusterNode.class);
        Whitebox.setInternalState(Constants.class, "ENTRY_NODE", clusterNode);

        // Mock throw a PriorityWaitException
        doThrow(new PriorityWaitException(111)).when(slot).fireEntry(context, resourceWrapper, node, 1, false);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        try {
            slot.entry(context, resourceWrapper, node, 1, false);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof PriorityWaitException);

            // Verify node,originNode,Constants.ENTRY_NODE counted threadNum, and not counted block_qps or anything else
            verify(node).increaseThreadNum();
            verify(node, never()).increaseBlockQps(1);
            verifyNoMoreInteractions(node);

            verify(originNode).increaseThreadNum();
            verify(originNode, never()).increaseBlockQps(1);
            verifyNoMoreInteractions(originNode);

            verify(clusterNode).increaseThreadNum();
            verify(clusterNode, never()).increaseBlockQps(1);
            verifyNoMoreInteractions(clusterNode);
        }
    }

    @Test
    public void testExit() throws Throwable {
        StatisticSlot slot = mock(StatisticSlot.class);
        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        when(resourceWrapper.getType()).thenReturn(EntryType.IN);
        DefaultNode node = mock(DefaultNode.class);
        when(context.getCurNode()).thenReturn(node);
        Entry entry = mock(Entry.class);
        when(context.getCurEntry()).thenReturn(entry);
        DefaultNode originNode = mock(DefaultNode.class);
        when(entry.getOriginNode()).thenReturn(originNode);
        ClusterNode clusterNode = mock(ClusterNode.class);
        Whitebox.setInternalState(Constants.class, "ENTRY_NODE", clusterNode);

        doCallRealMethod().when(slot).exit(context, resourceWrapper, 1);
        slot.exit(context, resourceWrapper, 1);

        // To verify call fireExit lastly
        InOrder inOrder = inOrder(slot, node, originNode, clusterNode);

        inOrder.verify(slot).exit(context, resourceWrapper, 1);

        // Verify node,originNode,Constants.ENTRY_NODE counted rt,success_pqs,threadNum, and not counted anything else
        inOrder.verify(node).addRtAndSuccess(anyLong(), eq(1));
        inOrder.verify(originNode).addRtAndSuccess(anyLong(), eq(1));

        inOrder.verify(node).decreaseThreadNum();
        inOrder.verify(originNode).decreaseThreadNum();
        verifyNoMoreInteractions(node, originNode);

        inOrder.verify(clusterNode).addRtAndSuccess(anyLong(), eq(1));
        inOrder.verify(clusterNode).decreaseThreadNum();
        verifyNoMoreInteractions(clusterNode);

        // Verify call fireExit lastly
        inOrder.verify(slot).fireExit(context, resourceWrapper, 1);
        verifyNoMoreInteractions(slot);

        inOrder.verifyNoMoreInteractions();
    }
}