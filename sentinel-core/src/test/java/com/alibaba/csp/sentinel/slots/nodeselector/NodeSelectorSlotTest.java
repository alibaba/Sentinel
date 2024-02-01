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
package com.alibaba.csp.sentinel.slots.nodeselector;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.CtEntryTestUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 * @author cdfive
 */
public class NodeSelectorSlotTest {

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
        NodeSelectorSlot slot = mock(NodeSelectorSlot.class);
        // Initialize the field in Mock Object to avoid NPE
        Whitebox.setInternalState(slot, "map", new HashMap<>());

        Context context = ContextUtil.enter("serviceA");
        ResourceWrapper resourceWrapper = new StringResourceWrapper("nodeA", EntryType.IN);

        Entry entry = mock(Entry.class);
        context.setCurEntry(entry);
        when(entry.getCurNode()).thenReturn(null);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, null, 1, false);
        slot.entry(context, resourceWrapper, null, 1, false);

        verify(slot).entry(context, resourceWrapper, null, 1, false);
        // Verify fireEntry method has been called, and only once
        // Use matchers here since the third parameter is a new defaultNode created in NodeSelectorSlot
        verify(slot).fireEntry(eq(context), eq(resourceWrapper), any(), eq(1), eq(false));
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testFireExit() throws Throwable {
        NodeSelectorSlot slot = mock(NodeSelectorSlot.class);
        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);

        doCallRealMethod().when(slot).exit(context, resourceWrapper, 1);
        slot.exit(context, resourceWrapper, 1);

        verify(slot).exit(context, resourceWrapper, 1);
        // Verify fireExit method has been called, and only once
        verify(slot).fireExit(context, resourceWrapper, 1);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testEntry() throws Throwable {
        NodeSelectorSlot slot = new NodeSelectorSlot();

        Context context = ContextUtil.enter("serviceA");
        ResourceWrapper resourceWrapper = new StringResourceWrapper("nodeA", EntryType.IN);
        // Set curEntry for context
        CtEntryTestUtil.buildCtEntry(resourceWrapper, null, context);

        assertNull(context.getCurNode());
        assertEquals(0, ((DefaultNode) context.getLastNode()).getChildList().size());

        slot.entry(context, resourceWrapper, null, 1, false);

        assertNotNull(context.getCurNode());
        assertEquals(1, ((DefaultNode) context.getLastNode()).getChildList().size());
    }

    @Test
    public void testSingleEntrance() throws Exception {
        final String contextName = "entry_SingleEntrance";
        ContextUtil.enter(contextName);

        EntranceNode entranceNode = null;
        for (Node node : Constants.ROOT.getChildList()) {
            entranceNode = (EntranceNode)node;
            if (entranceNode.getId().getName().equals(contextName)) {
                break;
            } else {
                System.out.println("Single entry: " + entranceNode.getId().getName());
            }
        }
        assertNotNull(entranceNode);
        assertTrue(entranceNode.getId().getName().equalsIgnoreCase(contextName));
        final String resName = "nodeA";
        Entry nodeA = SphU.entry(resName);

        Node curNode = ContextUtil.getContext().getCurNode();
        assertNotNull(curNode);
        assertEquals(resName, ((DefaultNode) curNode).getId().getName());

        DefaultNode defaultNode = null;

        int childListSize = entranceNode.getChildList().size();

        for (Node node : entranceNode.getChildList()) {
            if (((DefaultNode)node).getId().getName().equals(resName)) {
                defaultNode = (DefaultNode) node;
                break;
            }
        }

        // Verify the defaultNode in childList is not null
        assertNotNull(defaultNode);
        // Verify the defaultNode is same as the curNode in context
        assertSame(defaultNode, curNode);

        if (nodeA != null) {
            nodeA.exit();
        }

        ContextUtil.exit();

        // Same context and resource
        ContextUtil.enter(contextName);
        Entry nodeA2 = SphU.entry(resName);

        // Same resource, same context, since contextName is key, no new DefaultNode created
        assertSame(curNode, ContextUtil.getContext().getCurNode());

        // No new DefaultNode added to childList in entranceNode, the childListSize remain unchanged
        assertEquals(childListSize, entranceNode.getChildList().size());

        if (nodeA2 != null) {
            nodeA2.exit();
        }

        ContextUtil.exit();
    }

    @Test
    public void testMultipleEntrance() throws Exception {
        final String firstEntry = "entry_multiple_one";
        final String anotherEntry = "entry_multiple_another";
        final String resName = "nodeA";

        Node firstNode, anotherNode;
        ContextUtil.enter(firstEntry);
        Entry nodeA = SphU.entry(resName);
        firstNode = ContextUtil.getContext().getCurNode();
        if (nodeA != null) {
            nodeA.exit();
        }
        ContextUtil.exit();

        ContextUtil.enter(anotherEntry);
        nodeA = SphU.entry(resName);
        anotherNode = ContextUtil.getContext().getCurNode();
        if (nodeA != null) {
            nodeA.exit();
        }

        // Same resource, different context, since contextName is key, new DefaultNode will be created
        assertNotSame(firstNode, anotherNode);

        for (Node node : Constants.ROOT.getChildList()) {
            EntranceNode firstEntrance = (EntranceNode)node;
            if (firstEntrance.getId().getName().equals(firstEntry)) {
                assertEquals(1, firstEntrance.getChildList().size());
                for (Node child : firstEntrance.getChildList()) {
                    assertEquals(resName, ((DefaultNode)child).getId().getName());
                }
            } else if (firstEntrance.getId().getName().equals(anotherEntry)) {
                assertEquals(1, firstEntrance.getChildList().size());
                for (Node child : firstEntrance.getChildList()) {
                    assertEquals(resName, ((DefaultNode)child).getId().getName());
                }
            } else {
                System.out.println("Multiple entries: " + firstEntrance.getId().getName());
            }
        }
        ContextUtil.exit();
    }

    @Test
    public void testMultipleLayer() throws Exception {
        ContextUtil.enter("entry1", "appA");

        Entry nodeA = SphU.entry("nodeA");
        assertSame(ContextUtil.getContext().getCurEntry(), nodeA);

        DefaultNode dnA = (DefaultNode)nodeA.getCurNode();
        assertNotNull(dnA);
        assertSame("nodeA", dnA.getId().getName());

        Entry nodeB = SphU.entry("nodeB");
        assertSame(ContextUtil.getContext().getCurEntry(), nodeB);
        DefaultNode dnB = (DefaultNode)nodeB.getCurNode();
        assertNotNull(dnB);
        assertEquals(1, dnA.getChildList().size());
        assertTrue(dnA.getChildList().contains(dnB));

        Entry nodeC = SphU.entry("nodeC");
        assertSame(ContextUtil.getContext().getCurEntry(), nodeC);
        DefaultNode dnC = (DefaultNode)nodeC.getCurNode();
        assertNotNull(dnC);
        assertEquals(1, dnB.getChildList().size());
        assertTrue(dnB.getChildList().contains(dnC));

        if (nodeC != null) {
            nodeC.exit();
        }
        assertSame(ContextUtil.getContext().getCurEntry(), nodeB);

        if (nodeB != null) {
            nodeB.exit();
        }
        assertSame(ContextUtil.getContext().getCurEntry(), nodeA);

        if (nodeA != null) {
            nodeA.exit();
        }
        assertNull(ContextUtil.getContext().getCurEntry());
        ContextUtil.exit();

        // After exit by node and context, the node tree structure still remains
        for (Node node : Constants.ROOT.getChildList()) {
            EntranceNode entranceNode = (EntranceNode) node;
            if ("entry1".equals(entranceNode.getId().getName())) {
                entranceNode.getChildList().contains(dnA);
                assertTrue(dnA.getChildList().contains(dnB));
                assertTrue(dnB.getChildList().contains(dnC));
                break;
            }
        }
    }
}