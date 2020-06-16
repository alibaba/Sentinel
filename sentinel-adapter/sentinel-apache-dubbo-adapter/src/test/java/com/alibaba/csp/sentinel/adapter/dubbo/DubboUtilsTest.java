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

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;

import static com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig.DUBBO_INTERFACE_GROUP_VERSION_ENABLED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author cdfive
 */
public class DubboUtilsTest {

    @Before
    public void setUp() {
        SentinelConfig.setConfig("csp.sentinel.dubbo.resource.use.prefix", "true");
        SentinelConfig.setConfig(DubboConfig.DUBBO_PROVIDER_PREFIX, "");
        SentinelConfig.setConfig(DubboConfig.DUBBO_CONSUMER_PREFIX, "");
        SentinelConfig.setConfig(DUBBO_INTERFACE_GROUP_VERSION_ENABLED, "false");
    }


    @After
    public void tearDown() {
        SentinelConfig.setConfig("csp.sentinel.dubbo.resource.use.prefix", "false");
        SentinelConfig.setConfig(DubboConfig.DUBBO_PROVIDER_PREFIX, "");
        SentinelConfig.setConfig(DubboConfig.DUBBO_CONSUMER_PREFIX, "");
        SentinelConfig.setConfig(DUBBO_INTERFACE_GROUP_VERSION_ENABLED, "false");
    }


    @Test
    public void testGetApplication() {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getAttachments()).thenReturn(new HashMap<>());
        when(invocation.getAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY, ""))
                .thenReturn("consumerA");

        String application = DubboUtils.getApplication(invocation, "");
        verify(invocation).getAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY, "");

        assertEquals("consumerA", application);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetApplicationNoAttachments() {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getAttachments()).thenReturn(null);
        when(invocation.getAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY, ""))
                .thenReturn("consumerA");

        DubboUtils.getApplication(invocation, "");

        fail("No attachments in invocation, IllegalArgumentException should be thrown!");
    }

    @Test
    public void testGetResourceName() throws NoSuchMethodException {
        Invoker invoker = mock(Invoker.class);
        when(invoker.getInterface()).thenReturn(DemoService.class);

        Invocation invocation = mock(Invocation.class);
        Method method = DemoService.class.getDeclaredMethod("sayHello", String.class, int.class);
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());

        String resourceName = DubboUtils.getResourceName(invoker, invocation);

        assertEquals("com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);

    }

    @Test
    public void testGetResourceNameWithGroupAndVersion() throws NoSuchMethodException {
        Invoker invoker = mock(Invoker.class);
        URL url = URL.valueOf("dubbo://127.0.0.1:2181")
                .addParameter(CommonConstants.VERSION_KEY, "1.0.0")
                .addParameter(CommonConstants.GROUP_KEY, "grp1")
                .addParameter(CommonConstants.INTERFACE_KEY, DemoService.class.getName());
        when(invoker.getUrl()).thenReturn(url);
        when(invoker.getInterface()).thenReturn(DemoService.class);

        Invocation invocation = mock(Invocation.class);
        Method method = DemoService.class.getDeclaredMethod("sayHello", String.class, int.class);
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());

        String resourceNameUseGroupAndVersion = DubboUtils.getResourceName(invoker, invocation, true);

        assertEquals("com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:1.0.0:grp1:sayHello(java.lang.String,int)", resourceNameUseGroupAndVersion);
    }


    @Test
    public void testGetResourceNameWithPrefix() throws NoSuchMethodException {
        Invoker invoker = mock(Invoker.class);
        when(invoker.getInterface()).thenReturn(DemoService.class);

        Invocation invocation = mock(Invocation.class);
        Method method = DemoService.class.getDeclaredMethod("sayHello", String.class, int.class);
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());

        //test with default prefix
        String resourceName = DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboProviderPrefix());
        assertEquals("dubbo:provider:com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);
        resourceName = DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboConsumerPrefix());
        assertEquals("dubbo:consumer:com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);


        //test with custom prefix
        SentinelConfig.setConfig(DubboConfig.DUBBO_PROVIDER_PREFIX, "my:dubbo:provider:");
        SentinelConfig.setConfig(DubboConfig.DUBBO_CONSUMER_PREFIX, "my:dubbo:consumer:");
        resourceName = DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboProviderPrefix());
        assertEquals("my:dubbo:provider:com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);
        resourceName = DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboConsumerPrefix());
        assertEquals("my:dubbo:consumer:com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);

    }

    @Test
    public void testGetInterfaceName() {

        URL url = URL.valueOf("dubbo://127.0.0.1:2181")
                .addParameter(CommonConstants.VERSION_KEY, "1.0.0")
                .addParameter(CommonConstants.GROUP_KEY, "grp1")
                .addParameter(CommonConstants.INTERFACE_KEY, DemoService.class.getName());
        Invoker invoker = mock(Invoker.class);
        when(invoker.getUrl()).thenReturn(url);
        when(invoker.getInterface()).thenReturn(DemoService.class);

        SentinelConfig.setConfig(DUBBO_INTERFACE_GROUP_VERSION_ENABLED, "false");
        assertEquals("com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService", DubboUtils.getInterfaceName(invoker));

        SentinelConfig.setConfig(DUBBO_INTERFACE_GROUP_VERSION_ENABLED, "true");
        assertEquals("com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:1.0.0:grp1", DubboUtils.getInterfaceName(invoker));

    }
}