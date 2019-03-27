package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author cdfive
 */
public class AbstractDubboFilterTest {

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
}
