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

import com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author lianglin
 */
public class DubboTestUtil {


    public static Class<?> DEFAULT_TEST_SERVICE = DemoService.class;
    public static Method DEFAULT_TEST_METHOD_ONE = DEFAULT_TEST_SERVICE.getMethods()[0];
    public static Method DEFAULT_TEST_METHOD_TWO = DEFAULT_TEST_SERVICE.getMethods()[1];

    public static Invoker getMockInvoker(URL url, Class<?> cls) {
        Invoker invoker = mock(Invoker.class);
        when(invoker.getUrl()).thenReturn(url);
        when(invoker.getInterface()).thenReturn(cls);
        return invoker;
    }

    public static Invoker getDefaultMockInvoker() {
        return getMockInvoker(getDefaultTestURL(), DEFAULT_TEST_SERVICE);
    }

    public static Invocation getMockInvocation(Method method) {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());
        return invocation;
    }

    public static Invocation getDefaultMockInvocationOne() {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMethodName()).thenReturn(DEFAULT_TEST_METHOD_ONE.getName());
        when(invocation.getParameterTypes()).thenReturn(DEFAULT_TEST_METHOD_ONE.getParameterTypes());
        return invocation;
    }

    public static Invocation getDefaultMockInvocationTwo() {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMethodName()).thenReturn(DEFAULT_TEST_METHOD_TWO.getName());
        when(invocation.getParameterTypes()).thenReturn(DEFAULT_TEST_METHOD_TWO.getParameterTypes());
        return invocation;
    }

    public static URL getDefaultTestURL() {
        URL url = URL.valueOf("dubbo://127.0.0.1:2181")
                .addParameter(CommonConstants.VERSION_KEY, "1.0.0")
                .addParameter(CommonConstants.GROUP_KEY, "grp1")
                .addParameter(CommonConstants.INTERFACE_KEY, DEFAULT_TEST_SERVICE.getName());
        return url;
    }


}
