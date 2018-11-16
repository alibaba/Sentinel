package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.context.NullContext;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Zhao
 */
public class CtEntryTest {

    @Test
    public void testExitNotMatchCurEntry() {
        String contextName = "context-rpc";
        ContextUtil.enter(contextName);
        Context context = ContextUtil.getContext();
        CtEntry entry1 = null;
        CtEntry entry2 = null;
        try {
            entry1 = new CtEntry(new StringResourceWrapper("res1", EntryType.IN),
                null, ContextUtil.getContext());
            assertSame(entry1, context.getCurEntry());
            entry2 = new CtEntry(new StringResourceWrapper("res2", EntryType.IN),
                null, ContextUtil.getContext());
            assertSame(entry2, context.getCurEntry());

            // Forget to exit for entry 2...
            // Directly exit for entry 1, then boom...
            entry1.exit();
        } catch (ErrorEntryFreeException ex) {
            assertNotNull(entry1);
            assertNotNull(entry2);
            assertNull(entry1.context);
            assertNull(entry2.context);
            assertNull(context.getCurEntry());
            return;
        } finally {
            ContextUtil.exit();
        }
        fail("Mismatch entry-exit should throw an ErrorEntryFreeException");
    }

    private Context getFakeDefaultContext() {
        return new Context(null, Constants.CONTEXT_DEFAULT_NAME);
    }

    @Test
    public void testExitLastEntryWithDefaultContext() {
        final Context defaultContext = getFakeDefaultContext();
        ContextUtil.runOnContext(defaultContext, new Runnable() {
            @Override
            public void run() {
                CtEntry entry = new CtEntry(new StringResourceWrapper("res", EntryType.IN),
                    null, ContextUtil.getContext());
                assertSame(entry, defaultContext.getCurEntry());
                assertSame(defaultContext, ContextUtil.getContext());
                entry.exit();
                assertNull(defaultContext.getCurEntry());
                // Default context will be automatically exited.
                assertNull(ContextUtil.getContext());
            }
        });

    }

    @Test
    public void testExitTwoLastEntriesWithCustomContext() {
        String contextName = "context-rpc";
        ContextUtil.enter(contextName);
        Context context = ContextUtil.getContext();
        try {
            CtEntry entry1 = new CtEntry(new StringResourceWrapper("resA", EntryType.IN),
                null, context);
            entry1.exit();
            assertEquals(context, ContextUtil.getContext());
            CtEntry entry2 = new CtEntry(new StringResourceWrapper("resB", EntryType.IN),
                null, context);
            entry2.exit();
            assertEquals(context, ContextUtil.getContext());
        } finally {
            ContextUtil.exit();
            assertNull(ContextUtil.getContext());
        }
    }

    @Test
    public void testEntryAndExitWithNullContext() {
        Context context = new NullContext();
        CtEntry entry = new CtEntry(new StringResourceWrapper("testEntryAndExitWithNullContext", EntryType.IN),
            null, context);
        assertNull(context.getCurEntry());
        entry.exit();
        assertNull(context.getCurEntry());
        // Won't true exit, so the context won't be cleared.
        assertEquals(context, entry.context);
    }

    @Test
    public void testGetLastNode() {
        Context context = new NullContext();
        CtEntry entry = new CtEntry(new StringResourceWrapper("testGetLastNode", EntryType.IN),
            null, context);
        assertNull(entry.parent);
        assertNull(entry.getLastNode());
        Entry parentEntry = mock(Entry.class);
        Node node = mock(Node.class);
        when(parentEntry.getCurNode()).thenReturn(node);
        entry.parent = parentEntry;
        assertSame(node, entry.getLastNode());
    }

    @Before
    public void setUp() throws Exception {
        ContextTestUtil.cleanUpContext();
        ContextTestUtil.resetContextMap();
    }

    @After
    public void tearDown() throws Exception {
        ContextTestUtil.cleanUpContext();
        ContextTestUtil.resetContextMap();
    }
}