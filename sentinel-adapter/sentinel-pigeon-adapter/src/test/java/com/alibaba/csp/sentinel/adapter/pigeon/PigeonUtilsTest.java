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

import com.alibaba.csp.sentinel.adapter.pigeon.provider.DemoService;
import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Jobs Wang
 */
public class PigeonUtilsTest {

    @Test
    public void testGetResourceName() {
        InvocationRequest invocationRequest = mock(InvocationRequest.class);
        InvocationContext context = mock(InvocationContext.class);
        Method method = DemoService.class.getMethods()[0];

        when(context.getRequest()).thenReturn(invocationRequest);
        when(invocationRequest.getServiceName()).thenReturn(DemoService.class.getName());
        when(invocationRequest.getMethodName()).thenReturn(method.getName());
        String[] paramClazzName = new String[2];
        int i = 0;
        for (Class<?> clazz : method.getParameterTypes()) {
            paramClazzName[i] = clazz.getName();
            i ++;
        }
        when(invocationRequest.getParamClassName()).thenReturn(paramClazzName);

        String resourceName = PigeonUtils.getResourceName(context);

        assertEquals("com.alibaba.csp.sentinel.adapter.pigeon.provider.DemoService:sayHello(java.lang.String,int)", resourceName);
    }

}
