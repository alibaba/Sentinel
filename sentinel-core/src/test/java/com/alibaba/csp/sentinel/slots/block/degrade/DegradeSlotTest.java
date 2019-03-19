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
package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

/**
 * @author cdfive
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DegradeRuleManager.class)
public class DegradeSlotTest {

    @Test
    public void testFireEntry() throws Throwable {
        DegradeSlot slot = mock(DegradeSlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        when(resourceWrapper.getName()).thenReturn("");
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
        DegradeSlot slot = mock(DegradeSlot.class);

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
    public void testEntry() throws Throwable {
        PowerMockito.mockStatic(DegradeRuleManager.class);

        DegradeSlot slot = mock(DegradeSlot.class);

        Context context = mock(Context.class);
        ResourceWrapper resourceWrapper = mock(ResourceWrapper.class);
        DefaultNode node = mock(DefaultNode.class);

        doCallRealMethod().when(slot).entry(context, resourceWrapper, node, 1, false);
        slot.entry(context, resourceWrapper, node, 1, false);

        // Verify DegradeRuleManager.checkDegrade(...) method has been called, and only once
        PowerMockito.verifyStatic(DegradeRuleManager.class);
        DegradeRuleManager.checkDegrade(resourceWrapper, context, node, 1);
        PowerMockito.verifyNoMoreInteractions(DegradeRuleManager.class);
    }
}