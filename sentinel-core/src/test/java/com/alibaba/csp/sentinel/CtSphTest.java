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

    @Test
    public void testCustomContextEntryWithFullContextSize() {
        String resourceName = "testCustomContextSyncEntryWithFullContextSize";
        fillFullContext();
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        String contextName = "custom-context-" + System.currentTimeMillis();
        ContextUtil.enter(contextName, "9527");

        // Prepare a slot that "should not pass". If entered the slot, exception will be thrown.
        addShouldNotPassSlotFor(resourceWrapper);

        Entry entry = null;
        try {
                entry = ctSph.entry(resourceWrapper, 1);
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
    public void testDefaultContextEntryWithFullContextSize() {
        String resourceName = "testDefaultContextSyncEntryWithFullContextSize";
        fillFullContext();
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);

        // Prepare a slot that "should pass".
        ShouldPassSlot slot = addShouldPassSlotFor(resourceWrapper);
        assertFalse(slot.entered || slot.exited);

        Entry entry = null;
        try {
            entry = ctSph.entry(resourceWrapper, 1);
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
    public void testEntryEntryWhenSwitchOff() {
        // Turn off the switch.
        Constants.ON = false;

        String resourceNameA = "resSync";
        ResourceWrapper resourceWrapperA = new StringResourceWrapper(resourceNameA, EntryType.IN);

        // Prepare a slot that "should not pass". If entered the slot, exception will be thrown.
        addShouldNotPassSlotFor(resourceWrapperA);

        Entry entry = null;
        try {
            entry = ctSph.entry(resourceWrapperA, 1);
        } catch (BlockException ex) {
            fail("Unexpected blocked: " + ex.getClass().getCanonicalName());
        } finally {
            if (entry != null) {
                entry.exit();
            }
            Constants.ON = true;
        }
    }

    @Test
    public void testEntryAmountExceededForSyncEntry() {
        fillFullResources();
        Entry entry = null;
        try {
            entry = ctSph.entry("testSync", EntryType.IN, 1);
            assertSame(entry, ContextUtil.getContext().getCurEntry());
        } catch (BlockException ex) {
            fail("Unexpected blocked: " + ex.getClass().getCanonicalName());
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
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
            CtSph.getChainMap().put(resourceWrapper, new DefaultProcessorSlotChain());
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
                          Object... args) {
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
                          Object... args) throws Throwable {
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
                          Object... args) {
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