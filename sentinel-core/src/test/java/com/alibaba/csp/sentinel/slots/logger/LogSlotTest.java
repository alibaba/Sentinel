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
package com.alibaba.csp.sentinel.slots.logger;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author cdfive
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(EagleEyeLogUtil.class)
public class LogSlotTest {

    @Test
    public void testFireEntry() throws Throwable {
        LogSlot slot = mock(LogSlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        DefaultNode node = mock(DefaultNode.class);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        slot.entry(context, resourceWrapper, node, 1, false);

        verify(slot).entry(context, resourceWrapper, node, 1, false);
        // Verify fireEntry method has been called, and only once
        verify(slot).fireEntry(context, resourceWrapper, node, 1, false);
        verifyNoMoreInteractions(slot);
    }

    @Test
    public void testFireExit() throws Throwable {
        LogSlot slot = mock(LogSlot.class);

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
    public void testEntryBlockException() throws Throwable {
        PowerMockito.mockStatic(EagleEyeLogUtil.class);

        LogSlot slot = mock(LogSlot.class);

        Context context = mock(Context.class);
        when(context.getOrigin()).thenReturn("originA");
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        when(resourceWrapper.getName()).thenReturn("resourceA");
        DefaultNode node = mock(DefaultNode.class);

        // Mock throw a BlockException, e.g. FlowException
        doThrow(new FlowException("test")).when(slot).fireEntry(context, resourceWrapper, node, 1, false);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        try {
            slot.entry(context, resourceWrapper, node, 1, false);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof BlockException);

            // Verify EagleEyeLogUtil.log(...) method has been called, and only once
            PowerMockito.verifyStatic(EagleEyeLogUtil.class);
            EagleEyeLogUtil.log("resourceA", throwable.getClass().getSimpleName(), "test", "originA", 1);
            PowerMockito.verifyNoMoreInteractions(EagleEyeLogUtil.class);
        }
    }
}
