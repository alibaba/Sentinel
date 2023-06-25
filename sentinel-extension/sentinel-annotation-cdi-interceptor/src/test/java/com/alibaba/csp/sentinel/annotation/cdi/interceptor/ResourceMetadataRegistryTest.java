/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.annotation.cdi.interceptor;

import com.alibaba.csp.sentinel.annotation.cdi.interceptor.integration.service.FooService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Eric Zhao
 */
public class ResourceMetadataRegistryTest {

    @Before
    public void setUp() throws Exception {
        ResourceMetadataRegistry.clearBlockHandlerMap();
        ResourceMetadataRegistry.clearFallbackMap();
    }

    @After
    public void tearDown() throws Exception {
        ResourceMetadataRegistry.clearBlockHandlerMap();
        ResourceMetadataRegistry.clearFallbackMap();
    }

    @Test
    public void testUpdateThenLookupFallback() {
        Class<?> clazz = FooService.class;
        String methodName = "someMethodFallback";
        Method method = clazz.getMethods()[0];
        assertThat(ResourceMetadataRegistry.lookupFallback(clazz, methodName)).isNull();

        ResourceMetadataRegistry.updateFallbackFor(clazz, methodName, null);
        assertThat(ResourceMetadataRegistry.lookupFallback(clazz, methodName).isPresent()).isFalse();

        ResourceMetadataRegistry.updateFallbackFor(clazz, methodName, method);
        MethodWrapper wrapper = ResourceMetadataRegistry.lookupFallback(clazz, methodName);
        assertThat(wrapper.isPresent()).isTrue();
        assertThat(wrapper.getMethod()).isSameAs(method);
    }

    @Test
    public void testUpdateThenLookupBlockHandler() {
        Class<?> clazz = FooService.class;
        String methodName = "someMethodBlockHand;er";
        Method method = clazz.getMethods()[1];
        assertThat(ResourceMetadataRegistry.lookupBlockHandler(clazz, methodName)).isNull();

        ResourceMetadataRegistry.updateBlockHandlerFor(clazz, methodName, null);
        assertThat(ResourceMetadataRegistry.lookupBlockHandler(clazz, methodName).isPresent()).isFalse();

        ResourceMetadataRegistry.updateBlockHandlerFor(clazz, methodName, method);
        MethodWrapper wrapper = ResourceMetadataRegistry.lookupBlockHandler(clazz, methodName);
        assertThat(wrapper.isPresent()).isTrue();
        assertThat(wrapper.getMethod()).isSameAs(method);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateBlockHandlerBadArgument() {
        ResourceMetadataRegistry.updateBlockHandlerFor(null, "sxs", String.class.getMethods()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFallbackBadArgument() {
        ResourceMetadataRegistry.updateBlockHandlerFor(String.class, "", String.class.getMethods()[0]);
    }
}
