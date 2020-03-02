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
package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alipay.sofa.rpc.core.request.SofaRequest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link SofaRpcUtils}.
 *
 * @author cdfive
 */
public class SofaRpcUtilsTest {

    @Test
    public void testGetApplicationName() {
        SofaRequest request = new SofaRequest();
        String applicationName = SofaRpcUtils.getApplicationName(request);
        assertEquals("", applicationName);

        request.addRequestProp("app", "test-app");
        applicationName = SofaRpcUtils.getApplicationName(request);
        assertEquals("test-app", applicationName);
    }

    @Test
    public void testGetInterfaceResourceName() {
        SofaRequest request = new SofaRequest();
        request.setInterfaceName("com.alibaba.csp.sentinel.adapter.sofa.rpc.service.DemoService");
        String interfaceResourceName = SofaRpcUtils.getInterfaceResourceName(request);
        assertEquals("com.alibaba.csp.sentinel.adapter.sofa.rpc.service.DemoService", interfaceResourceName);
    }

    @Test
    public void testGetMethodResourceName() {
        SofaRequest request = new SofaRequest();
        request.setInterfaceName("com.alibaba.csp.sentinel.adapter.sofa.rpc.service.DemoService");
        request.setMethodName("sayHello");
        request.setMethodArgSigs(new String[]{"java.lang.String", "int"});
        String methodResourceName = SofaRpcUtils.getMethodResourceName(request);
        assertEquals("com.alibaba.csp.sentinel.adapter.sofa.rpc.service.DemoService#sayHello(java.lang.String,int)", methodResourceName);
    }

    @Test
    public void testGetMethodArguments() {
        SofaRequest request = new SofaRequest();
        request.setMethodArgs(new Object[]{"Sentinel", 2020});
        Object[] arguments = SofaRpcUtils.getMethodArguments(request);
        assertEquals(arguments.length, 2);
        assertEquals("Sentinel", arguments[0]);
        assertEquals(2020, arguments[1]);
    }
}
