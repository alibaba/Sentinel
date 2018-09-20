package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link AsyncEntry}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class AsyncEntryTest {

    @Test
    public void testCleanCurrentEntryInLocal() {
        final String contextName = "abc";
        try {
            ContextUtil.enter(contextName);
            Context curContext = ContextUtil.getContext();
            Entry previousEntry = new CtEntry(new StringResourceWrapper("entry-sync", EntryType.IN),
                null, curContext);
            AsyncEntry entry = new AsyncEntry(new StringResourceWrapper("testCleanCurrentEntryInLocal", EntryType.OUT),
                null, curContext);

            assertSame(entry, curContext.getCurEntry());

            entry.cleanCurrentEntryInLocal();
            assertNotSame(entry, curContext.getCurEntry());
            assertSame(previousEntry, curContext.getCurEntry());
        } finally {
            ContextTestUtil.cleanUpContext();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testCleanCurrentEntryInLocalError() {
        final String contextName = "abc";
        try {
            ContextUtil.enter(contextName);
            Context curContext = ContextUtil.getContext();
            AsyncEntry entry = new AsyncEntry(new StringResourceWrapper("testCleanCurrentEntryInLocal", EntryType.OUT),
                null, curContext);

            entry.cleanCurrentEntryInLocal();

            entry.cleanCurrentEntryInLocal();
        } finally {
            ContextTestUtil.cleanUpContext();
        }
    }

    @Test
    public void testInitAndGetAsyncContext() {
        final String contextName = "abc";
        final String origin = "xxx";
        try {
            ContextUtil.enter(contextName, origin);
            Context curContext = ContextUtil.getContext();
            AsyncEntry entry = new AsyncEntry(new StringResourceWrapper("testInitAndGetAsyncContext", EntryType.OUT),
                null, curContext);
            assertNull(entry.getAsyncContext());

            entry.initAsyncContext();

            Context asyncContext = entry.getAsyncContext();
            assertNotNull(asyncContext);
            assertEquals(contextName, asyncContext.getName());
            assertEquals(origin, asyncContext.getOrigin());
            assertSame(curContext.getEntranceNode(), asyncContext.getEntranceNode());
            assertSame(entry, asyncContext.getCurEntry());
            assertTrue(asyncContext.isAsync());
        } finally {
            ContextTestUtil.cleanUpContext();
        }
    }

    @Test
    public void testDuplicateInitAsyncContext() {
        Context context = new Context(null, "abc");
        AsyncEntry entry = new AsyncEntry(new StringResourceWrapper("testDuplicateInitAsyncContext", EntryType.OUT),
            null, context);
        entry.initAsyncContext();
        Context asyncContext = entry.getAsyncContext();

        // Duplicate init.
        entry.initAsyncContext();
        assertSame(asyncContext, entry.getAsyncContext());
    }

    @After
    public void tearDown() {
        ContextTestUtil.cleanUpContext();
    }
}