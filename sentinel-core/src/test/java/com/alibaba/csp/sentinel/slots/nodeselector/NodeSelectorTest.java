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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.EntranceNode;
import com.alibaba.csp.sentinel.node.Node;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class NodeSelectorTest {

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

        assertNotNull(ContextUtil.getContext().getCurNode());
        assertEquals(resName, ((DefaultNode)ContextUtil.getContext().getCurNode()).getId().getName());
        boolean hasNode = false;
        for (Node node : entranceNode.getChildList()) {
            if (((DefaultNode)node).getId().getName().equals(resName)) {
                hasNode = true;
            }
        }
        assertTrue(hasNode);

        if (nodeA != null) {
            nodeA.exit();
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

    //@Test
    public void testMultipleLayer() throws Exception {
        // TODO: fix this
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
        assertTrue(dnA.getChildList().contains(dnB));

        Entry nodeC = SphU.entry("nodeC");
        assertSame(ContextUtil.getContext().getCurEntry(), nodeC);
        DefaultNode dnC = (DefaultNode)nodeC.getCurNode();
        assertNotNull(dnC);
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

    }

}
