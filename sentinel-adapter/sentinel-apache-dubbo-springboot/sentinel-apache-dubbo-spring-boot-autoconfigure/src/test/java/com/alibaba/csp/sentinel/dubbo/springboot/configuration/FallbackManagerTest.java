/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dubbo.springboot.configuration;

import com.alibaba.csp.sentinel.dubbo.springboot.api.DemoService;
import com.alibaba.csp.sentinel.dubbo.springboot.api.impl.DemoServiceFallback;
import com.alibaba.csp.sentinel.dubbo.springboot.utils.ResourceUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FallbackManagerTest {

    private FallbackManager fallbackManager = new FallbackManager();
    private URL url = mock(URL.class);
    private Invoker invoker = mock(Invoker.class);
    private Invocation invocation = mock(Invocation.class);
    private Method method;


    @Before
    public void setUp() throws NoSuchMethodException {
        fallbackManager.getFallbackImplMap().clear();
        fallbackManager.getFallbackMethod().clear();
        when(url.getServiceInterface()).thenReturn(DemoService.class.getName());
        when(invoker.getUrl()).thenReturn(url);
        when(invoker.getInterface()).thenReturn(DemoService.class);
        method = DemoServiceFallback.class.getDeclaredMethod("test");
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());
    }


    @Test
    public void testHandle() {
        Result result1 = fallbackManager.handle(invoker, invocation, null);
        assertTrue(result1.hasException());
        testSetFallbackMethod();
        testSetFallbackImpl();
        Result result2 = fallbackManager.handle(invoker, invocation, null);
        assertFalse(result2.hasException());
        assertEquals("fallback", result2.getValue());
    }

    @Test
    public void testSetFallbackMethod() {
        String resourceName = ResourceUtils.getResourceName(url, method.getName(), method.getParameterTypes(), false, "");
        fallbackManager.setFallbackMethod(resourceName, method);
        assertEquals(1, fallbackManager.getFallbackMethod().size());
    }

    @Test
    public void testSetFallbackImpl() {
        fallbackManager.setFallbackImpl(ResourceUtils.getInterfaceResourceName(url), new FallbackManager.FallbackImplSupplier(DemoServiceFallback::new));
        assertEquals(1, fallbackManager.getFallbackImplMap().size());
    }
}