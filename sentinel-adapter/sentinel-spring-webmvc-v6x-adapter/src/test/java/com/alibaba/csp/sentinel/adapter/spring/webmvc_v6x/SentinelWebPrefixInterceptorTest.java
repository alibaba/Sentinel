/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.SentinelWebMvcConfig;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

public class SentinelWebPrefixInterceptorTest {

    @Test
    public void testPreHandle_asyncEntry() throws Exception {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setRequestAttributeName(null);
        SentinelWebPrefixInterceptor interceptor = new SentinelWebPrefixInterceptor();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a/b");
        req.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/a/b");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        try (MockedStatic<SphU> mocked = mockStatic(SphU.class)) {
            mocked.when(() -> SphU.asyncEntry(
                    eq("GET:/a/b"),
                    eq(ResourceTypeConstants.COMMON_WEB),
                    eq(EntryType.IN)
            )).thenCallRealMethod();
            interceptor.preHandle(req, resp, null);
            mocked.verify(() -> SphU.asyncEntry(
                    eq("GET:/a/b"),
                    eq(ResourceTypeConstants.COMMON_WEB),
                    eq(EntryType.IN)
            ));
        }
    }
}
