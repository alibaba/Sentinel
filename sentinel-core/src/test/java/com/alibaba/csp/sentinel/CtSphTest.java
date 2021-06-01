package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextTestUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.DefaultProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.SlotChainProvider;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for Sentinel internal {@link CtSph}.
 *
 * @author Eric Zhao
 */
public class CtSphTest {

    private final CtSph ctSph = new CtSph();

    private void testCustomContextEntryWithFullContextSize(String resourceName, boolean async) {
        fillFullContext();
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        String contextName = "custom-context-" + System.currentTimeMillis();
        ContextUtil.enter(contextName, "9527");

        // Prepare a slot that "should not pass". If entered the slot, exception will be thrown.
        addShouldNotPassSlotFor(resourceWrapper);

        Entry entry = null;
        try {
            if (async) {
                entry = ctSph.asyncEntry(resourceName, resourceWrapper.getEntryType(), 1);
            } else {
                entry = ctSph.entry(resourceWrapper, 1);
            }
        } catch (BlockException ex) {
            fail("Unexpected blocked: " + ex.getClass().getCanonicalName());
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    @Test
    public void testCustomContextSyncEntryWithFullContextSize() {
        String resourceName = "testCustomContextSyncEntryWithFullContextSize";
        testCustomContextEntryWithFullContextSize(resourceName, false);
    }

    @Test
    public void testCustomContextAsyncEntryWithFullContextSize() {
        String resourceName = "testCustomContextAsyncEntryWithFullContextSize";
        testCustomContextEntryWithFullContextSize(resourceName, true);
    }

    private void testDefaultContextEntryWithFullContextSize(String resourceName, boolean async) {
        fillFullContext();
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);

        // Prepare a slot that "should pass".
        ShouldPassSlot slot = addShouldPassSlotFor(resourceWrapper);
        assertFalse(slot.entered || slot.exited);

        Entry entry = null;
        try {
            if (!async) {
                entry = ctSph.entry(resourceWrapper, 1);
            } else {
                entry = ctSph.asyncEntry(resourceName, resourceWrapper.getEntryType(), 1);
                Context asyncContext = ((AsyncEntry)entry).getAsyncContext();
                assertTrue(ContextUtil.isDefaultContext(asyncContext));
                assertTrue(asyncContext.isAsync());
            }
            assertTrue(ContextUtil.isDefaultContext(ContextUtil.getContext()));
            assertTrue(slot.entered);
        } catch (BlockException ex) {
            fail("Unexpected blocked: " + ex.getClass().getCanonicalName());
        } finally {
            if (entry != null) {
                entry.exit();
                assertTrue(slot.exited);
            }
        }
    }

    @Test
    public void testDefaultContextSyncEntryWithFullContextSize() {
        String resourceName = "testDefaultContextSyncEntryWithFullContextSize";
        testDefaultContextEntryWithFullContextSize(resourceName, false);
    }

    @Test
    public void testDefaultContextAsyncEntryWithFullContextSize() {
        String resourceName = "testDefaultContextAsyncEntryWithFullContextSize";
        testDefaultContextEntryWithFullContextSize(resourceName, true);
    }

    @Test
    public void testEntryAndAsyncEntryWhenSwitchOff() {
        // Turn off the switch.
        Constants.ON = false;

        String resourceNameA = "resSync";
        String resourceNameB = "resAsync";
        ResourceWrapper resourceWrapperA = new StringResourceWrapper(resourceNameA, EntryType.IN);
        ResourceWrapper resourceWrapperB = new StringResourceWrapper(resourceNameB, EntryType.IN);

        // Prepare a slot that "should not pass". If entered the slot, exception will be thrown.
        addShouldNotPassSlotFor(resourceWrapperA);
        addShouldNotPassSlotFor(resourceWrapperB);

        Entry entry = null;
        AsyncEntry asyncEntry = null;
        try {
            entry = ctSph.entry(resourceWrapperA, 1);
            asyncEntry = ctSph.asyncEntry(resourceNameB, resourceWrapperB.getEntryType(), 1);
        } catch (BlockException ex) {
            fail("Unexpected blocked: " + ex.getClass().getCanonicalName());
        } finally {
            if (asyncEntry != null) {
                asyncEntry.exit();
            }
            if (entry != null) {
                entry.exit();
            }
            Constants.ON = true;
        }
    }

    @Test
    public void testAsyncEntryNormalPass() {
        String resourceName = "testAsyncEntryNormalPass";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        AsyncEntry entry = null;

        // Prepare a slot that "should pass".
        ShouldPassSlot slot = addShouldPassSlotFor(resourceWrapper);
        assertFalse(slot.entered || slot.exited);

        ContextUtil.enter("abc");
        Entry previousEntry = ContextUtil.getContext().getCurEntry();
        try {
            entry = ctSph.asyncEntry(resourceName, EntryType.IN, 1);
            assertTrue(slot.entered);
            assertFalse(slot.exited);
            Context asyncContext = entry.getAsyncContext();
            assertNotNull(asyncContext);
            assertSame(entry, asyncContext.getCurEntry());
            assertNotSame("The async entry should not be added to current context",
                entry, ContextUtil.getContext().getCurEntry());
            assertSame(previousEntry, ContextUtil.getContext().getCurEntry());
        } catch (BlockException ex) {
            fail("Unexpected blocked: " + ex.getClass().getCanonicalName());
        } finally {
            if (entry != null) {
                Context asyncContext = entry.getAsyncContext();
                entry.exit();
                assertTrue(slot.exited);
                assertNull(entry.getAsyncContext());
                assertSame(previousEntry, asyncContext.getCurEntry());
            }
            ContextUtil.exit();
        }
    }

    @Test
    public void testAsyncEntryNestedInSyncEntryNormalBlocked() {
        String previousResourceName = "fff";
        String resourceName = "testAsyncEntryNestedInSyncEntryNormalBlocked";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);

        // Prepare a slot that "must block".
        MustBlockSlot slot = addMustBlockSlot(resourceWrapper);
        assertFalse(slot.exited);
        // Previous entry should pass.
        addShouldPassSlotFor(new StringResourceWrapper(previousResourceName, EntryType.IN));
        ContextUtil.enter("bcd-" + System.currentTimeMillis());

        AsyncEntry entry = null;
        Entry syncEntry = null;
        Entry previousEntry = null;
        try {
            // First enter a sync resource.
            syncEntry = ctSph.entry(previousResourceName, EntryType.IN, 1);
            // Record current entry (previous for next).
            previousEntry = ContextUtil.getContext().getCurEntry();
            // Then enter an async resource.
            entry = ctSph.asyncEntry(resourceName, EntryType.IN, 1);

            // Should not pass here.
        } catch (BlockException ex) {
            assertNotNull(previousEntry);
            assertNull(entry);
            assertTrue(slot.exited);
            assertSame(previousEntry, ContextUtil.getContext().getCurEntry());
            return;
        } finally {
            assertNull(entry);
            assertNotNull(syncEntry);

            syncEntry.exit();
            ContextUtil.exit();
        }
        fail("This async entry is expected to be blocked");
    }

    private void testEntryAmountExceeded(boolean async) {
        fillFullResources();
        Entry entry = null;
        try {
            if (!async) {
                entry = ctSph.entry("testSync", EntryType.IN, 1);
            } else {
                entry = ctSph.asyncEntry("testSync", EntryType.IN, 1);
            }
            assertNull(((CtEntry)entry).chain);
            if (!async) {
                assertSame(entry, ContextUtil.getContext().getCurEntry());
            } else {
                Context asyncContext = ((AsyncEntry)entry).getAsyncContext();
                assertNotNull(asyncContext);
                assertSame(entry, asyncContext.getCurEntry());
            }
        } catch (BlockException ex) {
            fail("Unexpected blocked: " + ex.getClass().getCanonicalName());
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Test
    public void testEntryAmountExceededForSyncEntry() {
        testEntryAmountExceeded(false);
    }

    @Test
    public void testEntryAmountExceededForAsyncEntry() {
        testEntryAmountExceeded(true);
    }

    @Test
    public void testLookUpSlotChain() {
        ResourceWrapper r1 = new StringResourceWrapper("firstRes", EntryType.IN);
        assertFalse(CtSph.getChainMap().containsKey(r1));
        ProcessorSlot<Object> chainR1 = ctSph.lookProcessChain(r1);
        assertNotNull("The slot chain for r1 should be created", chainR1);
        assertSame("Should return the cached slot chain once it has been created", chainR1, ctSph.lookProcessChain(r1));

        fillFullResources();
        ResourceWrapper r2 = new StringResourceWrapper("secondRes", EntryType.IN);
        assertFalse(CtSph.getChainMap().containsKey(r2));
        assertNull("The slot chain for r2 should not be created because amount exceeded", ctSph.lookProcessChain(r2));
        assertNull(ctSph.lookProcessChain(r2));
    }

    private void fillFullContext() {
        for (int i = 0; i < Constants.MAX_CONTEXT_NAME_SIZE; i++) {
            ContextUtil.enter("test-context-" + i);
            ContextUtil.exit();
        }
    }

    private void fillFullResources() {
        for (int i = 0; i < Constants.MAX_SLOT_CHAIN_SIZE; i++) {
            ResourceWrapper resourceWrapper = new StringResourceWrapper("test-resource-" + i, EntryType.IN);
            CtSph.getChainMap().put(resourceWrapper, SlotChainProvider.newSlotChain());
        }
    }

    private void addShouldNotPassSlotFor(ResourceWrapper resourceWrapper) {
        ProcessorSlotChain slotChain = new DefaultProcessorSlotChain();
        slotChain.addLast(new ShouldNotPassSlot());
        CtSph.getChainMap().put(resourceWrapper, slotChain);
    }

    private ShouldPassSlot addShouldPassSlotFor(ResourceWrapper resourceWrapper) {
        ProcessorSlotChain slotChain = new DefaultProcessorSlotChain();
        ShouldPassSlot shouldPassSlot = new ShouldPassSlot();
        slotChain.addLast(shouldPassSlot);
        CtSph.getChainMap().put(resourceWrapper, slotChain);
        return shouldPassSlot;
    }

    private MustBlockSlot addMustBlockSlot(ResourceWrapper resourceWrapper) {
        ProcessorSlotChain slotChain = new DefaultProcessorSlotChain();
        MustBlockSlot mustBlockSlot = new MustBlockSlot();
        slotChain.addLast(mustBlockSlot);
        CtSph.getChainMap().put(resourceWrapper, slotChain);
        return mustBlockSlot;
    }

    private class ShouldNotPassSlot extends AbstractLinkedProcessorSlot<DefaultNode> {
        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode param, int count,
                          boolean prioritized, Object... args) {
            throw new IllegalStateException("Should not enter this slot!");
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
            throw new IllegalStateException("Should not exit this slot!");
        }
    }

    private class MustBlockSlot extends AbstractLinkedProcessorSlot<DefaultNode> {
        boolean exited = false;

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode param, int count,
                          boolean prioritized, Object... args) throws Throwable {
            throw new BlockException("custom") {};
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
            exited = true;
        }
    }

    private class ShouldPassSlot extends AbstractLinkedProcessorSlot<DefaultNode> {
        boolean entered = false;
        boolean exited = false;

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode param, int count,
                          boolean prioritized, Object... args) {
            entered = true;
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
            exited = true;
        }
    }

    @Before
    public void setUp() throws Exception {
        ContextTestUtil.cleanUpContext();
        ContextTestUtil.resetContextMap();
        CtSph.resetChainMap();
    }

    @After
    public void tearDown() throws Exception {
        ContextTestUtil.cleanUpContext();
        ContextTestUtil.resetContextMap();
        CtSph.resetChainMap();
    }
}