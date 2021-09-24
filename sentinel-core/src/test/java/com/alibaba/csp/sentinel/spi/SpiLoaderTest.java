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
package com.alibaba.csp.sentinel.spi;

import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.metric.extension.MetricCallbackInit;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.SlotChainBuilder;
import com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder;
import com.alibaba.csp.sentinel.slots.block.authority.AuthoritySlot;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot;
import com.alibaba.csp.sentinel.slots.block.flow.FlowSlot;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.slots.logger.LogSlot;
import com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot;
import com.alibaba.csp.sentinel.slots.statistic.StatisticSlot;
import com.alibaba.csp.sentinel.slots.system.SystemSlot;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test cases for {@link SpiLoader}.
 *
 * @author cdfive
 */
public class SpiLoaderTest {

    @Before
    public void setUp() {
        SpiLoader.resetAndClearAll();
    }

    @Before
    public void tearDown() {
        SpiLoader.resetAndClearAll();
    }

    @Test
    public void testCreateSpiLoader() {
        SpiLoader slotLoader1 = SpiLoader.of(ProcessorSlot.class);
        assertNotNull(slotLoader1);

        SpiLoader slotLoader2 = SpiLoader.of(ProcessorSlot.class);
        assertNotNull(slotLoader2);

        assertSame(slotLoader1, slotLoader2);

        SpiLoader initFuncLoader1 = SpiLoader.of(InitFunc.class);
        assertNotNull(initFuncLoader1);
        assertNotSame(slotLoader1, initFuncLoader1);
        assertNotEquals(slotLoader1, initFuncLoader1);

        SpiLoader<InitFunc> initFuncLoader2 = SpiLoader.of(InitFunc.class);
        assertNotNull(initFuncLoader2);

        assertSame(initFuncLoader1, initFuncLoader2);
    }

    @Test
    public void testCreateSpiLoaderNotInterface() {
        try {
            SpiLoader.of(SphU.class);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertThat(e.getMessage(), containsString("must be interface or abstract class"));
        }
    }

    @Test
    public void testLoadInstanceList() {
        SpiLoader spiLoader = SpiLoader.of(ProcessorSlot.class);
        List<ProcessorSlot> slots1 = spiLoader.loadInstanceList();
        List<ProcessorSlot> slots2 = spiLoader.loadInstanceList();
        assertNotSame(slots1, slots2);

        List<Class<? extends ProcessorSlot>> prototypeSlotClasses = new ArrayList<>(2);
        prototypeSlotClasses.add(NodeSelectorSlot.class);
        prototypeSlotClasses.add(ClusterBuilderSlot.class);

        List<Class<? extends ProcessorSlot>> singletonSlotClasses = new ArrayList<>(6);
        singletonSlotClasses.add(LogSlot.class);
        singletonSlotClasses.add(StatisticSlot.class);
        singletonSlotClasses.add(AuthoritySlot.class);
        singletonSlotClasses.add(SystemSlot.class);
        singletonSlotClasses.add(FlowSlot.class);
        singletonSlotClasses.add(DegradeSlot.class);

        for (int i = 0; i < slots1.size(); i++) {
            ProcessorSlot slot1 = slots1.get(i);
            ProcessorSlot slot2 = slots2.get(i);
            assertSame(slot1.getClass(), slot2.getClass());

            boolean found = false;
            for (Class<? extends ProcessorSlot> prototypeSlotClass : prototypeSlotClasses) {
                if (prototypeSlotClass.equals(slot1.getClass())) {
                    found = true;
                    assertTrue(prototypeSlotClass.equals(slot2.getClass()));
                    // Verify prototype function
                    assertNotSame(slot1, slot2);
                    break;
                }
            }

            if (found) {
                continue;
            }

            for (Class<? extends ProcessorSlot> singletonSlotClass : singletonSlotClasses) {
                if (singletonSlotClass.equals(slot1.getClass())) {
                    found = true;
                    assertTrue(singletonSlotClass.equals(slot2.getClass()));
                    // Verify single function
                    assertSame(slot1, slot2);
                    break;
                }
            }

            if (!found) {
                fail("Should found and not go through here");
            }
        }
    }

    @Test
    public void testLoadInstanceListSorted() {
        List<ProcessorSlot> sortedSlots = SpiLoader.of(ProcessorSlot.class).loadInstanceListSorted();
        assertNotNull(sortedSlots);

        // Total 8 default slot in sentinel-core
        assertEquals(8, sortedSlots.size());

        // Verify the order of slot
        int index = 0;
        assertTrue(sortedSlots.get(index++) instanceof NodeSelectorSlot);
        assertTrue(sortedSlots.get(index++) instanceof ClusterBuilderSlot);
        assertTrue(sortedSlots.get(index++) instanceof LogSlot);
        assertTrue(sortedSlots.get(index++) instanceof StatisticSlot);
        assertTrue(sortedSlots.get(index++) instanceof AuthoritySlot);
        assertTrue(sortedSlots.get(index++) instanceof SystemSlot);
        assertTrue(sortedSlots.get(index++) instanceof FlowSlot);
        assertTrue(sortedSlots.get(index++) instanceof DegradeSlot);
    }

    @Test
    public void testLoadHighestPriorityInstance() {
        ProcessorSlot slot = SpiLoader.of(ProcessorSlot.class).loadHighestPriorityInstance();
        assertNotNull(slot);

        // NodeSelectorSlot is highest order priority with @Spi(order = -10000) among all slots
        assertTrue(slot instanceof NodeSelectorSlot);
    }

    @Test
    public void testLoadLowestPriorityInstance() {
        ProcessorSlot slot = SpiLoader.of(ProcessorSlot.class).loadLowestPriorityInstance();
        assertNotNull(slot);

        // NodeSelectorSlot is lowest order priority with @Spi(order = -1000) among all slots
        assertTrue(slot instanceof DegradeSlot);
    }

    @Test
    public void testLoadFirstInstance() {
        ProcessorSlot slot = SpiLoader.of(ProcessorSlot.class).loadFirstInstance();
        assertNotNull(slot);
        assertTrue(slot instanceof NodeSelectorSlot);

        SlotChainBuilder chainBuilder = SpiLoader.of(SlotChainBuilder.class).loadFirstInstance();
        assertNotNull(chainBuilder);
        assertTrue(chainBuilder instanceof SlotChainBuilder);

        InitFunc initFunc = SpiLoader.of(InitFunc.class).loadFirstInstance();
        assertNotNull(initFunc);
        assertTrue(initFunc instanceof MetricCallbackInit);
    }

    @Test
    public void testLoadFirstInstanceOrDefault() {
        SlotChainBuilder slotChainBuilder = SpiLoader.of(SlotChainBuilder.class).loadFirstInstanceOrDefault();
        assertNotNull(slotChainBuilder);
        assertTrue(slotChainBuilder instanceof DefaultSlotChainBuilder);
    }

    @Test
    public void testLoadDefaultInstance() {
        SlotChainBuilder slotChainBuilder = SpiLoader.of(SlotChainBuilder.class).loadDefaultInstance();
        assertNotNull(slotChainBuilder);
        assertTrue(slotChainBuilder instanceof DefaultSlotChainBuilder);
    }

    @Test
    public void testLoadInstanceByClass() {
        ProcessorSlot slot = SpiLoader.of(ProcessorSlot.class).loadInstance(StatisticSlot.class);
        assertNotNull(slot);
        assertTrue(slot instanceof StatisticSlot);
    }

    @Test
    public void testLoadInstanceByAliasName() {
        ProcessorSlot slot = SpiLoader.of(ProcessorSlot.class).loadInstance("com.alibaba.csp.sentinel.slots.statistic.StatisticSlot");
        assertNotNull(slot);
        assertTrue(slot instanceof StatisticSlot);
    }

    @Test
    public void testToString() {
        SpiLoader spiLoader = SpiLoader.of(ProcessorSlot.class);
        assertEquals("com.alibaba.csp.sentinel.spi.SpiLoader[com.alibaba.csp.sentinel.slotchain.ProcessorSlot]"
                , spiLoader.toString());
    }

    /**
     * Following test cases are for some test Interfaces.
     */
    @Test
    public void test_TestNoSpiFileInterface() {
        SpiLoader<TestNoSpiFileInterface> loader = SpiLoader.of(TestNoSpiFileInterface.class);

        List<TestNoSpiFileInterface> providers = loader.loadInstanceList();
        assertTrue(providers.size() == 0);

        List<TestNoSpiFileInterface> sortedProviders = loader.loadInstanceListSorted();
        assertTrue(sortedProviders.size() == 0);

        TestNoSpiFileInterface firstProvider = loader.loadFirstInstance();
        assertNull(firstProvider);

        TestNoSpiFileInterface defaultProvider = loader.loadDefaultInstance();
        assertNull(defaultProvider);
    }

    @Test
    public void test_TestNoProviderInterface() {
        List<TestNoProviderInterface> providers = SpiLoader.of(TestNoProviderInterface.class).loadInstanceList();
        assertTrue(providers.size() == 0);
    }

    @Test
    public void test_TestInterface() {
        SpiLoader<TestInterface> loader = SpiLoader.of(TestInterface.class);

        List<TestInterface> providers = loader.loadInstanceList();
        assertTrue(providers.size() == 4);
        assertTrue(providers.get(0) instanceof TestOneProvider);
        assertTrue(providers.get(1) instanceof TestTwoProvider);
        assertTrue(providers.get(2) instanceof TestThreeProvider);
        assertTrue(providers.get(3) instanceof TestFiveProvider);

        List<TestInterface> sortedProviders = loader.loadInstanceListSorted();
        assertEquals(sortedProviders.size(), 4);
        assertTrue(sortedProviders.get(0) instanceof TestThreeProvider);
        assertTrue(sortedProviders.get(1) instanceof TestFiveProvider);
        assertTrue(sortedProviders.get(2) instanceof TestTwoProvider);
        assertTrue(sortedProviders.get(3) instanceof TestOneProvider);

        assertSame(providers.get(0), sortedProviders.get(3));
        assertSame(providers.get(1), sortedProviders.get(2));
        assertNotSame(providers.get(2), sortedProviders.get(0));
        assertSame(providers.get(3), sortedProviders.get(1));

        assertTrue(loader.loadDefaultInstance() instanceof TestFiveProvider);

        assertTrue(loader.loadHighestPriorityInstance() instanceof TestThreeProvider);
        assertTrue(loader.loadLowestPriorityInstance() instanceof TestOneProvider);

        assertTrue(loader.loadInstance("two") instanceof TestTwoProvider);
        assertSame(loader.loadInstance("two"), loader.loadInstance("two"));

        try {
            loader.loadInstance("one");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SpiLoaderException);
            assertThat(e.getMessage(), containsString("no Provider class's aliasName is one"));
        }

        TestInterface oneProvider1 = loader.loadInstance(TestOneProvider.class);
        assertNotNull(oneProvider1);
        TestInterface oneProvider2 = loader.loadInstance(TestOneProvider.class);
        assertNotNull(oneProvider2);
        assertSame(oneProvider1, oneProvider2);

        try {
            loader.loadInstance(TestInterface.class);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SpiLoaderException);
            assertThat(e.getMessage(), containsString("is not subtype of"));
        }

        try {
            loader.loadInstance(TestFourProvider.class);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SpiLoaderException);
            assertThat(e.getMessage(), allOf(containsString("is not Provider class of")
                , containsString("check if it is in the SPI configuration file?")));
        }
    }
}
