package com.alibaba.csp.sentinel.adapter.dubbo.origin;

import com.alibaba.csp.sentinel.adapter.dubbo.DubboUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcInvocation;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author tc
 * @date 2020/6/10
 */
public class DubboOriginRegistryTest {

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultOriginFail() {
        DubboOriginRegistry.getDubboOrigin().handler(null, null);
    }

    @Test
    public void testDefaultOriginSuccess() {
        RpcInvocation invocation = new RpcInvocation();
        String dubboName = "sentinel";
        invocation.setAttachment(DubboUtils.DUBBO_APPLICATION_KEY, dubboName);
        String origin = DubboOriginRegistry.getDubboOrigin().handler(null, invocation);
        Assert.assertEquals(dubboName, origin);
    }

    @Test
    public void testCustomOrigin() {
        DubboOriginRegistry.setDubboOrigin(new DubboOrigin() {
            @Override
            public String handler(Invoker<?> invoker, Invocation invocation) {
                return invocation.getAttachment(DubboUtils.DUBBO_APPLICATION_KEY, "default") + "_" + invocation
                    .getMethodName();
            }
        });

        RpcInvocation invocation = new RpcInvocation();
        String origin = DubboOriginRegistry.getDubboOrigin().handler(null, invocation);
        Assert.assertEquals("default_null", origin);

        String dubboName = "sentinel";
        invocation.setAttachment(DubboUtils.DUBBO_APPLICATION_KEY, dubboName);
        origin = DubboOriginRegistry.getDubboOrigin().handler(null, invocation);
        Assert.assertEquals(dubboName + "_null", origin);

        invocation.setMethodName("hello");
        origin = DubboOriginRegistry.getDubboOrigin().handler(null, invocation);
        Assert.assertEquals(dubboName + "_hello", origin);
    }

}
