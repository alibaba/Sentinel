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
package com.alibaba.csp.sentinel.adapter.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.AsyncEvent;

import org.junit.Test;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockHttpServletRequest;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

public class CommonAsyncListenerTest {
    @Test
    public void singleton() {
        CommonAsyncListener instance1 = CommonAsyncListener.getInstance();
        CommonAsyncListener instance2 = CommonAsyncListener.getInstance();
        assertEquals(instance1, instance2);
    }
    
    public void normal() throws BlockException {
        String url = "/test";
        Context ctx = ContextUtil.enter(url);
        Entry entry = SphU.entry(url);
        assertNotNull(ContextUtil.getContext());
        ContextUtil.exitByAsync();
        assertNull(ContextUtil.getContext());
        MockHttpServletRequest request = new MockHttpServletRequest("get", url);
        request.setAttribute(CommonFilter.ATTR_CONTEXT, ctx);
        request.setAttribute(CommonFilter.ATTR_ENTRY, entry);
        MockAsyncContext asyncContext = new MockAsyncContext(request, null);
        AsyncEvent event = new AsyncEvent(asyncContext, new RuntimeException());
        try {
            assertEquals(ctx.getCurEntry(), entry);
            // after running
            CommonAsyncListener.getInstance().onComplete(event);
            assertNull(ctx.getCurEntry());
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    
    @Test
    public void error() throws BlockException {
        String url = "/test";
        Context ctx = ContextUtil.enter(url);
        Entry entry = SphU.entry(url);
        MockHttpServletRequest request = new MockHttpServletRequest("get", url);
        request.setAttribute(CommonFilter.ATTR_CONTEXT, ctx);
        request.setAttribute(CommonFilter.ATTR_ENTRY, entry);
        MockAsyncContext asyncContext = new MockAsyncContext(request, null);
        AsyncEvent event = new AsyncEvent(asyncContext, new RuntimeException());
        try {
            assertEquals(ctx.getCurEntry(), entry);
            CommonAsyncListener.getInstance().onError(event);
            ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
            assertEquals(1, cn.exceptionQps(), 0.1);
            CommonAsyncListener.getInstance().onComplete(event);
            assertNull(ctx.getCurEntry());
            assertNotNull(ContextUtil.getContext());
            ContextUtil.exit();
            assertNull(ContextUtil.getContext());
            cn.reset();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    
    /**
     * 
     * @throws BlockException
     */
    @Test
    public void timeout() throws BlockException {
        String url = "/test";
        Context ctx = ContextUtil.enter(url);
        Entry entry = SphU.entry(url);
        MockHttpServletRequest request = new MockHttpServletRequest("get", url);
        request.setAttribute(CommonFilter.ATTR_CONTEXT, ctx);
        request.setAttribute(CommonFilter.ATTR_ENTRY, entry);
        MockAsyncContext asyncContext = new MockAsyncContext(request, null);
        AsyncEvent event = new AsyncEvent(asyncContext);
        try {
            assertEquals(ctx.getCurEntry(), entry);
            CommonAsyncListener.getInstance().onTimeout(event);
            ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
            assertEquals(1, cn.exceptionQps(), 0.1);
            CommonAsyncListener.getInstance().onComplete(event);
            assertNull(ctx.getCurEntry());
            assertNotNull(ContextUtil.getContext());
            ContextUtil.exit();
            assertNull(ContextUtil.getContext());
            cn.reset();
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    
    @Test
    public void noSuppliedRequest() throws BlockException {
        String url = "/test";
        Context ctx = ContextUtil.enter(url);
        Entry entry = SphU.entry(url);
        MockAsyncContext asyncContext = new MockAsyncContext(null, null);
        AsyncEvent event = new AsyncEvent(asyncContext);
        try {
            assertEquals(ctx.getCurEntry(), entry);
            CommonAsyncListener.getInstance().onTimeout(event);
            ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
            // no timeout traced
            assertEquals(0, cn.exceptionQps(), 0.1);
            CommonAsyncListener.getInstance().onComplete(event);
            assertNotNull(ctx.getCurEntry());
            // entry is not exited
            entry.exit();
            cn.reset();
            assertNull(ctx.getCurEntry());
            assertNotNull(ContextUtil.getContext());
            ContextUtil.exit();
            assertNull(ContextUtil.getContext());
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    
}
