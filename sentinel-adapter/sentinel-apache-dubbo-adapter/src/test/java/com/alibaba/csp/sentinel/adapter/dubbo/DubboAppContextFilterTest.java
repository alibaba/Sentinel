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
package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.BaseTest;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author cdfive
 */
public class DubboAppContextFilterTest extends BaseTest {

    private DubboAppContextFilter filter = new DubboAppContextFilter();

    @Before
    public void setUp() {
        cleanUpAll();
    }

    @After
    public void cleanUp() {
        cleanUpAll();
    }

    @Test
    public void testInvokeApplicationKey() {
        Invoker invoker = mock(Invoker.class);
        Invocation invocation = mock(Invocation.class);
        URL url = URL.valueOf("test://test:111/test?application=serviceA");
        when(invoker.getUrl()).thenReturn(url);

        filter.invoke(invoker, invocation);
        verify(invoker).invoke(invocation);

        String application = RpcContext.getContext().getAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY);
        assertEquals("serviceA", application);
    }

    @Test
    public void testInvokeNullApplicationKey() {
        Invoker invoker = mock(Invoker.class);
        Invocation invocation = mock(Invocation.class);
        URL url = URL.valueOf("test://test:111/test?application=");
        when(invoker.getUrl()).thenReturn(url);

        filter.invoke(invoker, invocation);
        verify(invoker).invoke(invocation);

        String application = RpcContext.getContext().getAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY);
        assertNull(application);
    }
}
