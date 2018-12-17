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
package com.alibaba.csp.sentinel.adapter.servlet.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import org.junit.Test;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockHttpServletRequest;

public class AsyncUtilTest {
    @Test
    public void testIsAsyncStarted() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertFalse(AsyncUtil.isAsyncStarted(request));
        request.setAsyncStarted(true);
        assertTrue(AsyncUtil.isAsyncStarted(request));
    }
    
    @Test
    public void testAddListener() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        try {
            AsyncUtil.addListener(null, null);
            AsyncUtil.addListener(null, request);
        } catch (Exception e) {
            assertTrue(false);
        }
        MockAsyncContext ctx = new MockAsyncContext(request, null);
        request.setAsyncContext(ctx);
        assertEquals(0, ctx.getListeners().size());
        AsyncUtil.addListener(new AsyncListener() {
            
            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
            }
            
            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
            }
            
            @Override
            public void onError(AsyncEvent event) throws IOException {
            }
            
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
            }
        }, request);
        assertEquals(1, ctx.getListeners().size());
    }
}
