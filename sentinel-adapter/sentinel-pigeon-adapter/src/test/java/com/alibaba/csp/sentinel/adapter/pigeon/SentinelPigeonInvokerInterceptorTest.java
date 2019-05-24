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
package com.alibaba.csp.sentinel.adapter.pigeon;

import com.alibaba.csp.sentinel.BaseTest;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.pigeon.provider.DemoService;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

/**
 * @author Jobs Wang
 */
public class SentinelPigeonInvokerInterceptorTest extends BaseTest {

    private SentinelPigeonInvokerInterceptor interceptor = new SentinelPigeonInvokerInterceptor();

    @Before
    public void setUp() {
        cleanUpAll();
    }

    @After
    public void cleanUp() {
        cleanUpAll();
    }

    @Test
    public void testPreInvoke() {
        final InvocationRequest invocationRequest = mock(InvocationRequest.class);
        final InvokerContext invokerContext = mock(InvokerContext.class);
        Method method = DemoService.class.getMethods()[0];

        when(invokerContext.getRequest()).thenReturn(invocationRequest);
        when(invocationRequest.getServiceName()).thenReturn(DemoService.class.getName());
        when(invocationRequest.getMethodName()).thenReturn(method.getName());
        String[] paramClazzName = new String[2];
        int i = 0;
        for (Class<?> clazz : method.getParameterTypes()) {
            paramClazzName[i] = clazz.getName();
            i ++;
        }
        when(invocationRequest.getParamClassName()).thenReturn(paramClazzName);

        interceptor.preInvoke(invokerContext);
    }

    @Test
    public void testPostInvoke() throws BlockException {
        final InvokerContext invokerContext = mock(InvokerContext.class);

        Method method = DemoService.class.getMethods()[0];
        String resourceName = MethodUtils.buildResource(method);
        SphU.entry(resourceName, EntryType.OUT);

        interceptor.postInvoke(invokerContext);
    }

    @Test
    public void testAfterThrowing() throws BlockException {
        final InvokerContext invokerContext = mock(InvokerContext.class);

        Method method = DemoService.class.getMethods()[0];
        String resourceName = MethodUtils.buildResource(method);
        SphU.entry(resourceName, EntryType.OUT);
        Throwable ex = new Throwable("error");

        interceptor.afterThrowing(invokerContext, ex);
    }

}
