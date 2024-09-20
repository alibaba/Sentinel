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
package com.alibaba.csp.sentinel.slots.clusterbuilder;

import com.alibaba.csp.sentinel.CtEntryTestUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.Env;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author jialiang.linjl
 * @author cdfive
 */
public class ClusterBuilderSlotTest {

    @Before
    public void setUp() {
        ClusterBuilderSlot.getClusterNodeMap().clear();
        ContextTestUtil.cleanUpContext();
    }

    @After
    public void cleanUp() {
        ClusterBuilderSlot.getClusterNodeMap().clear();
        ContextTestUtil.cleanUpContext();
    }

    @Test
    public void testFireEntry() throws Throwable {
        ClusterBuilderSlot slot = mock(ClusterBuilderSlot.class);

        Context context = ContextUtil.enter("serviceA");
        ResourceWrapper resourceWrapper = new StringResourceWrapper("nodeA", EntryType.IN);
        DefaultNode node = new DefaultNode(resourceWrapper, null);

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
        ClusterBuilderSlot slot = mock(ClusterBuilderSlot.class);
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
    public void testEntryNoneOrigin() throws Throwable {
        ClusterBuilderSlot slot = new ClusterBuilderSlot();

        Context context = ContextUtil.enter("serviceA");
        ResourceWrapper resourceWrapper = new StringResourceWrapper("nodeA", EntryType.IN);
        DefaultNode defaultNode = new DefaultNode(resourceWrapper, null);

        slot.entry(context, resourceWrapper, defaultNode, 1, false);

        assertEquals(1, ClusterBuilderSlot.getClusterNodeMap().size());
        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNodeMap().get(resourceWrapper);
        assertNotNull(clusterNode);
        assertSame(clusterNode, defaultNode.getClusterNode());

        assertEquals(0, clusterNode.getOriginCountMap().size());
    }

    @Test
    public void testEntryWithOrigin() throws Throwable {
        ClusterBuilderSlot slot = new ClusterBuilderSlot();

        Context context = ContextUtil.enter("serviceA", "originA");
        ResourceWrapper resourceWrapper = new StringResourceWrapper("nodeA", EntryType.IN);
        DefaultNode defaultNode = new DefaultNode(resourceWrapper, null);
        // Set curEntry for context
        CtEntryTestUtil.buildCtEntry(resourceWrapper, null, context);

        slot.entry(context, resourceWrapper, defaultNode, 1, false);

        assertEquals(1, ClusterBuilderSlot.getClusterNodeMap().size());
        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNodeMap().get(resourceWrapper);
        assertNotNull(clusterNode);
        assertSame(clusterNode, defaultNode.getClusterNode());

        assertEquals(1, clusterNode.getOriginCountMap().size());
        assertTrue(clusterNode.getOriginCountMap().containsKey("originA"));
    }

    @Test
    public void testSameResourceDifferentOrigin() throws Exception {
        ContextUtil.enter("entry1", "caller1");

        Entry nodeA = SphU.entry("nodeA");

        Node curNode = nodeA.getCurNode();
        assertSame(curNode.getClass(), DefaultNode.class);
        DefaultNode dN = (DefaultNode)curNode;
        assertTrue(dN.getClusterNode().getOriginCountMap().containsKey("caller1"));
        assertSame(nodeA.getOriginNode(), dN.getClusterNode().getOrCreateOriginNode("caller1"));

        if (nodeA != null) {
            nodeA.exit();
        }
        ContextUtil.exit();

        ContextUtil.enter("entry4", "caller2");

        nodeA = SphU.entry("nodeA");

        curNode = nodeA.getCurNode();
        assertSame(curNode.getClass(), DefaultNode.class);
        DefaultNode dN1 = (DefaultNode)curNode;
        assertTrue(dN1.getClusterNode().getOriginCountMap().containsKey("caller2"));
        assertNotSame(dN1, dN);
        // Same resource, same clusterNode
        assertSame(dN1.getClusterNode(), dN.getClusterNode());

        if (nodeA != null) {
            nodeA.exit();
        }
        ContextUtil.exit();
    }
}