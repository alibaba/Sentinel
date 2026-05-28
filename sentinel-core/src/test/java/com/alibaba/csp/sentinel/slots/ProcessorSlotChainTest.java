package com.alibaba.csp.sentinel.slots;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.DefaultProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import java.lang.reflect.Field;

/**
 * @author joyce
 * @date 2026/5/14
 */
public class ProcessorSlotChainTest {

    @Test
    public void testBeforeFix_nextOfSingletonIsShared() throws Exception {
        DefaultSlot singleton = new DefaultSlot();
        DefaultProcessorSlotChain chain1 = createLegacyChain();
        DefaultProcessorSlotChain chain2 = createLegacyChain();

        DefaultSlot prototypeA1 = new DefaultSlot();
        chain1.addLast(singleton);
        chain1.addLast(prototypeA1);

        DefaultSlot prototypeA2 = new DefaultSlot();
        chain2.addLast(singleton);
        chain2.addLast(prototypeA2);

        Assert.assertEquals(chain1.getNext().getNext(), chain2.getNext().getNext());
    }
    @Test
    public void testAfterFix_eachChainHasItsOwnWrapper() {
        DefaultSlot singleton = new DefaultSlot();
        DefaultProcessorSlotChain chain1 = new DefaultProcessorSlotChain();
        DefaultProcessorSlotChain chain2 = new DefaultProcessorSlotChain();

        chain1.addLast(singleton);
        chain1.addLast(new DefaultSlot());

        chain2.addLast(singleton);
        chain2.addLast(new DefaultSlot());
        Assert.assertNotEquals(chain1.getNext().getNext(), chain2.getNext().getNext());
    }

    private DefaultProcessorSlotChain createLegacyChain() throws Exception {
        DefaultProcessorSlotChain chain = Mockito.spy(new DefaultProcessorSlotChain());
        Field endField = DefaultProcessorSlotChain.class.getDeclaredField("end");
        endField.setAccessible(true);

        Mockito.doAnswer(invocation -> {
            AbstractLinkedProcessorSlot<?> slot = invocation.getArgument(0);
            AbstractLinkedProcessorSlot<?> end =
                    (AbstractLinkedProcessorSlot<?>) endField.get(chain);
            end.setNext(slot);
            endField.set(chain, slot);
            return null;
        }).when(chain).addLast(Mockito.any(AbstractLinkedProcessorSlot.class));

        return chain;
    }

    private static class DefaultSlot extends AbstractLinkedProcessorSlot<Object> {

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, Object t, int count,
                          boolean prioritized, Object... args) throws Throwable {
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        }
    }
}
