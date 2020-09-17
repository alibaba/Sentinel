/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.dubbo.origin;

import com.alibaba.csp.sentinel.adapter.dubbo.DubboUtils;
import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboAdapterGlobalConfig;
import com.alibaba.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author tiecheng
 */
public class DubboOriginRegistryTest {

    @After
    public void cleanUp() {
        DubboAdapterGlobalConfig.setOriginParser(new DefaultDubboOriginParser());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultOriginParserFail() {
        DubboAdapterGlobalConfig.getOriginParser().parse(null, null);
    }

    @Test
    public void testDefaultOriginParserSuccess() {
        RpcInvocation invocation = new RpcInvocation();
        String dubboName = "sentinel";
        invocation.setAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY, dubboName);
        String origin = DubboAdapterGlobalConfig.getOriginParser().parse(null, invocation);
        Assert.assertEquals(dubboName, origin);
    }

    @Test
    public void testCustomOriginParser() {
        DubboAdapterGlobalConfig.setOriginParser(new DubboOriginParser() {
            @Override
            public String parse(Invoker<?> invoker, Invocation invocation) {
                return invocation.getAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY, "default") + "_" + invocation
                        .getMethodName();
            }
        });

        RpcInvocation invocation = new RpcInvocation();
        String origin = DubboAdapterGlobalConfig.getOriginParser().parse(null, invocation);
        Assert.assertEquals("default_null", origin);

        String dubboName = "sentinel";
        invocation.setAttachment(DubboUtils.SENTINEL_DUBBO_APPLICATION_KEY, dubboName);
        origin = DubboAdapterGlobalConfig.getOriginParser().parse(null, invocation);
        Assert.assertEquals(dubboName + "_null", origin);

        invocation.setMethodName("hello");
        origin = DubboAdapterGlobalConfig.getOriginParser().parse(null, invocation);
        Assert.assertEquals(dubboName + "_hello", origin);
    }

}
