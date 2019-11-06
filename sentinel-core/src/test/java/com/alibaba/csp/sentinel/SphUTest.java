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
package com.alibaba.csp.sentinel;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Test cases for {@link SphU}.
 *
 * @author jialiang.linjl
 */
public class SphUTest {

    @Test
    public void testStringEntryNormal() throws BlockException {
        Entry e = SphU.entry("resourceName");

        assertNotNull(e);
        assertEquals(e.resourceWrapper.getName(), "resourceName");
        assertEquals(e.resourceWrapper.getEntryType(), EntryType.OUT);
        assertEquals(ContextUtil.getContext().getName(), Constants.CONTEXT_DEFAULT_NAME);

        e.exit();
    }

    @Test
    public void testMethodEntryNormal() throws BlockException, NoSuchMethodException, SecurityException {
        Method method = SphUTest.class.getMethod("testMethodEntryNormal");
        Entry e = SphU.entry(method);

        assertNotNull(e);
        assertTrue(StringUtil
            .equalsIgnoreCase(e.resourceWrapper.getName(),
                "com.alibaba.csp.sentinel.SphUTest:testMethodEntryNormal()"));
        assertEquals(e.resourceWrapper.getEntryType(), EntryType.OUT);
        assertEquals(ContextUtil.getContext().getName(), Constants.CONTEXT_DEFAULT_NAME);

        e.exit();
    }

    @Test(expected = ErrorEntryFreeException.class)
    public void testStringEntryNotPairedException() throws BlockException {
        Entry e = SphU.entry("resourceName");
        Entry e1 = SphU.entry("resourceName");

        if (e != null) {
            e.exit();
        }
        if (e1 != null) {
            e1.exit();
        }
    }

    @Test
    public void testStringEntryCount() throws BlockException {
        Entry e = SphU.entry("resourceName", 2);

        assertNotNull(e);
        assertEquals("resourceName", e.resourceWrapper.getName());
        assertEquals(e.resourceWrapper.getEntryType(), EntryType.OUT);
        assertEquals(ContextUtil.getContext().getName(), Constants.CONTEXT_DEFAULT_NAME);

        e.exit(2);
    }

    @Test
    public void testMethodEntryCount() throws BlockException, NoSuchMethodException, SecurityException {
        Method method = SphUTest.class.getMethod("testMethodEntryNormal");
        Entry e = SphU.entry(method, 2);

        assertNotNull(e);
        assertTrue(StringUtil
            .equalsIgnoreCase(e.resourceWrapper.getName(),
                "com.alibaba.csp.sentinel.SphUTest:testMethodEntryNormal()"));
        assertEquals(e.resourceWrapper.getEntryType(), EntryType.OUT);

        e.exit(2);
    }

    @Test
    public void testStringEntryType() throws BlockException {
        Entry e = SphU.entry("resourceName", EntryType.IN);

        assertSame(e.resourceWrapper.getEntryType(), EntryType.IN);

        e.exit();
    }

    @Test
    public void testMethodEntryType() throws BlockException, NoSuchMethodException, SecurityException {
        Method method = SphUTest.class.getMethod("testMethodEntryNormal");
        Entry e = SphU.entry(method, EntryType.IN);

        assertSame(e.resourceWrapper.getEntryType(), EntryType.IN);

        e.exit();
    }

    @Test
    public void testStringEntryCountType() throws BlockException {
        Entry e = SphU.entry("resourceName", EntryType.IN, 2);

        assertSame(e.resourceWrapper.getEntryType(), EntryType.IN);

        e.exit(2);
    }

    @Test
    public void testMethodEntryCountType() throws BlockException, NoSuchMethodException, SecurityException {
        Method method = SphUTest.class.getMethod("testMethodEntryNormal");
        Entry e = SphU.entry(method, EntryType.IN, 2);

        assertSame(e.resourceWrapper.getEntryType(), EntryType.IN);

        e.exit();
    }

    @Test
    public void testStringEntryAll() throws BlockException {
        final String arg0 = "foo";
        final String arg1 = "baz";
        Entry e = SphU.entry("resourceName", EntryType.IN, 2, arg0, arg1);
        assertSame(e.resourceWrapper.getEntryType(), EntryType.IN);

        e.exit(2, arg0, arg1);
    }

    @Test
    public void testMethodEntryAll() throws BlockException, NoSuchMethodException, SecurityException {
        final String arg0 = "foo";
        final String arg1 = "baz";
        Method method = SphUTest.class.getMethod("testMethodEntryNormal");
        Entry e = SphU.entry(method, EntryType.IN, 2, arg0, arg1);

        assertSame(e.resourceWrapper.getEntryType(), EntryType.IN);

        e.exit(2, arg0, arg1);
    }
}
