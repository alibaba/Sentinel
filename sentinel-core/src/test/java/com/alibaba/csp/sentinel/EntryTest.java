package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.util.function.BiConsumer;

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

        @Override
        public void whenTerminate(BiConsumer<Context, Entry> consumer) {
            // do nothing
        }
    }
}
