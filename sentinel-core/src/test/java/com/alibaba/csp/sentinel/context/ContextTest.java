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
package com.alibaba.csp.sentinel.context;

import com.alibaba.csp.sentinel.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link Context} and {@link ContextUtil}.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class ContextTest {

    @Before
    public void setUp() {
        resetContextMap();
    }

    @After
    public void cleanUp() {
        ContextTestUtil.cleanUpContext();
    }

    @Test
    public void testEnterCustomContextWhenExceedsThreshold() {
        fillContext();
        try {
            String contextName = "abc";
            ContextUtil.enter(contextName, "bcd");
            Context curContext = ContextUtil.getContext();
            assertNotEquals(contextName, curContext.getName());
            assertTrue(curContext instanceof NullContext);
            assertEquals("", curContext.getOrigin());
        } finally {
            ContextUtil.exit();
            resetContextMap();
        }
    }

    @Test
    public void testDefaultContextWhenExceedsThreshold() {
        fillContext();
        try {
            ContextUtil.trueEnter(Constants.CONTEXT_DEFAULT_NAME, "");
            Context curContext = ContextUtil.getContext();
            assertEquals(Constants.CONTEXT_DEFAULT_NAME, curContext.getName());
            assertNotNull(curContext.getEntranceNode());
        } finally {
            ContextUtil.exit();
            resetContextMap();
        }
    }

    private void fillContext() {
        for (int i = 0; i < Constants.MAX_CONTEXT_NAME_SIZE; i++) {
            ContextUtil.enter("test-context-" + i);
            ContextUtil.exit();
        }
    }

    private void resetContextMap() {
        ContextTestUtil.resetContextMap();
    }

    @Test
    public void testEnterContext() {
        final String contextName = "contextA";
        final String origin = "originA";
        ContextUtil.enter(contextName, origin);

        Context curContext = ContextUtil.getContext();
        assertEquals(contextName, curContext.getName());
        assertEquals(origin, curContext.getOrigin());
        assertFalse(curContext.isAsync());

        ContextUtil.exit();
        assertNull(ContextUtil.getContext());
    }

    @Test
    public void testReplaceContext() {
        final String contextName = "contextA";
        final String origin = "originA";
        ContextUtil.enter(contextName, origin);

        Context contextB = Context.newAsyncContext(null, "contextB")
            .setOrigin("originA");
        Context contextA = ContextUtil.replaceContext(contextB);
        assertEquals(contextName, contextA.getName());
        assertEquals(origin, contextA.getOrigin());
        assertFalse(contextA.isAsync());

        Context curContextAfterReplace = ContextUtil.getContext();
        assertEquals(contextB.getName(), curContextAfterReplace.getName());
        assertEquals(contextB.getOrigin(), curContextAfterReplace.getOrigin());
        assertTrue(curContextAfterReplace.isAsync());

        ContextUtil.replaceContext(null);
        assertNull(ContextUtil.getContext());
    }

    @Test
    public void testRunOnContext() {
        final String contextName = "contextA";
        final String origin = "originA";
        ContextUtil.enter(contextName, origin);

        final Context contextB = Context.newAsyncContext(null, "contextB")
            .setOrigin("originB");
        assertEquals(contextName, ContextUtil.getContext().getName());
        ContextUtil.runOnContext(contextB, new Runnable() {
            @Override
            public void run() {
                Context curContext = ContextUtil.getContext();
                assertEquals(contextB.getName(), curContext.getName());
                assertEquals(contextB.getOrigin(), curContext.getOrigin());
                assertTrue(curContext.isAsync());
            }
        });
        assertEquals(contextName, ContextUtil.getContext().getName());
    }
}
