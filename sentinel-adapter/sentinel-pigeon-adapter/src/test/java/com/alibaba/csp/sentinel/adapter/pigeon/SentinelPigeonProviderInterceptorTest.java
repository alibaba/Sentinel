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
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Jobs Wang
 */
public class SentinelPigeonProviderInterceptorTest extends BaseTest {

    private SentinelPigeonProviderInterceptor interceptor = new SentinelPigeonProviderInterceptor();

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
        final ProviderContext providerContext = mock(ProviderContext.class);
        final ServiceMethod serviceMethod = mock(ServiceMethod.class);
        Method method = DemoService.class.getMethods()[0];

        when(providerContext.getRequest()).thenReturn(invocationRequest);
        when(providerContext.getServiceMethod()).thenReturn(serviceMethod);
        when(serviceMethod.getMethod()).thenReturn(method);
        when(invocationRequest.getServiceName()).thenReturn(DemoService.class.getName());
        when(invocationRequest.getMethodName()).thenReturn(method.getName());
        String[] paramClazzName = new String[2];
        int i = 0;
        for (Class<?> clazz : method.getParameterTypes()) {
            paramClazzName[i] = clazz.getName();
            i ++;
        }
        when(invocationRequest.getParamClassName()).thenReturn(paramClazzName);

        interceptor.preInvoke(providerContext);

        Context context = ContextUtil.getContext();
        assertNotNull(context);
    }

    @Test
    public void testPostInvoke() throws BlockException {
        final ProviderContext providerContext = mock(ProviderContext.class);
        final ServiceMethod serviceMethod = mock(ServiceMethod.class);

        Method method = DemoService.class.getMethods()[0];
        String resourceName = MethodUtils.buildResource(method);
        ContextUtil.enter(resourceName);
        SphU.entry(resourceName, EntryType.IN, 1, method.getParameters());
        Throwable ex = new Throwable("service error!");

        when(providerContext.getServiceError()).thenReturn(ex);
        when(providerContext.getServiceMethod()).thenReturn(serviceMethod);
        when(serviceMethod.getMethod()).thenReturn(method);

        interceptor.postInvoke(providerContext);

//        Context context = ContextUtil.getContext();
//        assertNull(context);
    }

}
