/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dubbo.springboot.utils;

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import org.apache.dubbo.common.URL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ResourceUtilsTest {

    @Test
    public void getResourceName() {
        URL url = mock(URL.class);
        String interfaceName = "com.alibaba.dubbo.DemoService";
        String colonSeparatedKey = "com.alibaba.dubbo.DemoService:1.0.0:grp1";
        when(url.getColonSeparatedKey()).thenReturn(colonSeparatedKey);
        when(url.getServiceInterface()).thenReturn(interfaceName);
        String methodName = "test";
        Class[] classes = new Class[]{Object.class, Integer[].class};
        String resourceName = ResourceUtils.getResourceName(url, methodName, classes, true, "prefix");
        assertNotNull(resourceName);

    }

    @Test
    public void getInterfaceResourceName() {
        URL url = mock(URL.class);
        String interfaceName = "com.alibaba.dubbo.DemoService";
        String colonSeparatedKey = "com.alibaba.dubbo.DemoService:1.0.0:grp1";
        when(url.getColonSeparatedKey()).thenReturn(colonSeparatedKey);
        when(url.getServiceInterface()).thenReturn(interfaceName);
        assertEquals(interfaceName, ResourceUtils.getInterfaceResourceName(url));
        SentinelConfig.setConfig(DubboConfig.DUBBO_INTERFACE_GROUP_VERSION_ENABLED, "true");
        assertEquals(colonSeparatedKey, ResourceUtils.getInterfaceResourceName(url));
    }

}