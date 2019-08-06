package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author cdfive
 */
public class AbstractDubboFilterTest {


    @Before
    public void setUp() {
        SentinelConfig.setConfig("csp.sentinel.dubbo.resource.use.prefix", "true");
        SentinelConfig.setConfig(DubboConfig.DUBBO_PROVIDER_PREFIX, "");
        SentinelConfig.setConfig(DubboConfig.DUBBO_CONSUMER_PREFIX, "");
    }

    @After
    public void tearDown() {
        SentinelConfig.setConfig("csp.sentinel.dubbo.resource.use.prefix", "false");
        SentinelConfig.setConfig(DubboConfig.DUBBO_PROVIDER_PREFIX, "");
        SentinelConfig.setConfig(DubboConfig.DUBBO_CONSUMER_PREFIX, "");
    }

    private AbstractDubboFilter filter = new AbstractDubboFilter() {
        @Override
        public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
            return null;
        }
    };


    @Test
    public void testGetResourceName() {
        Invoker invoker = mock(Invoker.class);
        when(invoker.getInterface()).thenReturn(DemoService.class);

        Invocation invocation = mock(Invocation.class);
        Method method = DemoService.class.getMethods()[0];
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());

        String resourceName = filter.getResourceName(invoker, invocation);

        assertEquals("com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);
    }

    @Test
    public void testGetResourceNameWithPrefix() {
        Invoker invoker = mock(Invoker.class);
        when(invoker.getInterface()).thenReturn(DemoService.class);

        Invocation invocation = mock(Invocation.class);
        Method method = DemoService.class.getMethods()[0];
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());

        //test with default prefix
        String resourceName = filter.getResourceName(invoker, invocation, DubboConfig.getDubboProviderPrefix());
        System.out.println("resourceName =  " + resourceName);
        assertEquals("dubbo:provider:com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);
        resourceName = filter.getResourceName(invoker, invocation, DubboConfig.getDubboConsumerPrefix());
        assertEquals("dubbo:consumer:com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);


        //test with custom prefix
        SentinelConfig.setConfig(DubboConfig.DUBBO_PROVIDER_PREFIX, "my:dubbo:provider:");
        SentinelConfig.setConfig(DubboConfig.DUBBO_CONSUMER_PREFIX, "my:dubbo:consumer:");
        resourceName = filter.getResourceName(invoker, invocation, DubboConfig.getDubboProviderPrefix());
        assertEquals("my:dubbo:provider:com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);
        resourceName = filter.getResourceName(invoker, invocation, DubboConfig.getDubboConsumerPrefix());
        assertEquals("my:dubbo:consumer:com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService:sayHello(java.lang.String,int)", resourceName);

    }
}
