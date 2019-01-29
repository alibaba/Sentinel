/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link Entry}.
 *
 * @author Eric Zhao
 */
public class EntryTest {

    @Test
    public void testEntryExitCounts() {
        ResourceWrapper resourceWrapper = new StringResourceWrapper("resA", EntryType.IN);
        TestEntry entry = new TestEntry(resourceWrapper);
        entry.exit();
        assertEquals(-1, entry.count);
        entry.exit(9);
        assertEquals(-10, entry.count);
    }

    @Test
    public void testEntryFieldsGetSet() {
        ResourceWrapper resourceWrapper = new StringResourceWrapper("resA", EntryType.IN);
        Entry entry = new TestEntry(resourceWrapper);
        assertSame(resourceWrapper, entry.getResourceWrapper());
        Throwable error = new IllegalStateException();
        entry.setError(error);
        assertSame(error, entry.getError());
        Node curNode = mock(Node.class);
        entry.setCurNode(curNode);
        assertSame(curNode, entry.getCurNode());
        Node originNode = mock(Node.class);
        entry.setOriginNode(originNode);
        assertSame(originNode, entry.getOriginNode());
    }

    private class TestEntry extends Entry {

        int count = 0;

        TestEntry(ResourceWrapper resourceWrapper) {
            super(resourceWrapper);
        }

        @Override
        public void exit(int count, Object... args) throws ErrorEntryFreeException {
            this.count -= count;
        }

        @Override
        protected Entry trueExit(int count, Object... args) throws ErrorEntryFreeException {
            return null;
        }

        @Override
        public Node getLastNode() {
            return null;
        }
    }
}