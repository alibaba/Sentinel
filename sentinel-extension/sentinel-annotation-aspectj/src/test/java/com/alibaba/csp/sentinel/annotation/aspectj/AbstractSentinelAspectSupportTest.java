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
package com.alibaba.csp.sentinel.annotation.aspectj;

import com.alibaba.csp.sentinel.annotation.aspectj.integration.service.FooService;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Eric Zhao
 */
public class AbstractSentinelAspectSupportTest extends AbstractSentinelAspectSupport {

    @Test
    public void testGetResourceName() throws Exception {
        Method method = FooService.class.getMethod("random");
        String resourceName = "someRandom";
        String expectedResolvedName = FooService.class.getName() + ":random()";
        assertThat(getResourceName(resourceName, method)).isEqualTo(resourceName);
        assertThat(getResourceName(null, method)).isEqualTo(expectedResolvedName);
        assertThat(getResourceName("", method)).isEqualTo(expectedResolvedName);
    }

    @Test
    public void testGlobalFallbackDefaultIsNull() {
        assertThat(getGlobalFallback()).isNull();
    }

    @Test
    public void testSetAndGetGlobalFallback() throws Throwable {
        SentinelAnnotationGlobalFallback fallback = mock(SentinelAnnotationGlobalFallback.class);
        setGlobalFallback(fallback);
        assertThat(getGlobalFallback()).isSameAs(fallback);
    }

    @Test
    public void testClearGlobalFallback() {
        setGlobalFallback(mock(SentinelAnnotationGlobalFallback.class));
        setGlobalFallback(null);
        assertThat(getGlobalFallback()).isNull();
    }
}
