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
package com.alibaba.csp.sentinel.slots.block.degrade.param;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test cases for {@link ParamDegradeSlot}.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamDegradeSlotTest {

    ParamDegradeSlot paramDegradeSlot = new ParamDegradeSlot();

    @Test
    public void testEntryWhenParamDegradeRuleNotExists() throws Throwable {
        String resourceName = "testEntryWhenParamDegradeRuleNotExists";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        paramDegradeSlot.entry(null, resourceWrapper, null, 1, false, "abc");
    }


    @Test
    public void testEntryWhenParamDegradeExists() throws Throwable {
        String resourceName = "testEntryWhenParamDegradeExists";
        ResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.IN);
        long argToGo = 1L;
        ParamDegradeRule rule = new ParamDegradeRule(resourceName).setParamIdx(0);
        rule.setCount(10d);
        rule.setSlowRatioThreshold(0.9d);
        rule.setTimeWindow(20);
        rule.setStatIntervalMs(20000);
        rule.setMinRequestAmount(1);

        ParamDegradeItem item = new ParamDegradeItem();
        item.setObject("1");
        item.setCount(1d);
        item.setClassType("java.util.Long");

        rule.setParamDegradeItemList(Collections.singletonList(item));

        ParamDegradeRuleManager.loadRules(Collections.singletonList(rule));

        Context context = Mockito.mock(Context.class);
        Entry entry = Mockito.mock(Entry.class);
        Mockito.when(context.getCurEntry()).thenReturn(entry);

        paramDegradeSlot.setNext(new ExceptionSlot());
        // The first entry will pass.
        paramDegradeSlot.entry(context, resourceWrapper, null, 1, false, argToGo);
        paramDegradeSlot.exit(context, resourceWrapper, 1, argToGo);
        // The second entry will be blocked.
        try {
            paramDegradeSlot.entry(context, resourceWrapper, null, 1, false, argToGo);
            fail("The second entry should be blocked");
        } catch (DegradeException ex) {
            assertEquals(resourceName, ex.getRule().getResource());
        }

        long argToContinue = 10L;
        try {
            paramDegradeSlot.entry(context, resourceWrapper, null, 1, false, argToContinue);
        } catch (DegradeException ex) {
            fail("The second entry should not be blocked");
        }
    }

    public class ExceptionSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

        @Override
        public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, boolean prioritized, Object... args)
                throws Throwable {
            Thread.sleep(1000);
        }

        @Override
        public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
            System.out.println("Exiting for entry on ExceptionSlot: ");
        }
    }

    @Before
    public void setUp() {
        ParamDegradeRuleManager.loadRules(null);
    }

    @After
    public void tearDown() {
        // Clean the metrics map.
        ParamDegradeRuleManager.loadRules(null);
    }
}
