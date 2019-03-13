package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.BaseTest;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
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

        String application = RpcContext.getContext().getAttachment(DubboUtils.DUBBO_APPLICATION_KEY);
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

        String application = RpcContext.getContext().getAttachment(DubboUtils.DUBBO_APPLICATION_KEY);
        assertNull(application);
    }
}
