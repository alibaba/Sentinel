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
import com.alibaba.csp.sentinel.annotation.aspectj.integration.service.FooUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Zhao
 * @author dowenliu-xyz(hawkdowen@hotmail.com)
 */
public class ResourceMetadataRegistryTest {

    @Before
    public void setUp() {
        ResourceMetadataRegistry.clearBlockHandlerMap();
        ResourceMetadataRegistry.clearFallbackMap();
        ResourceMetadataRegistry.clearDefaultFallbackMap();
    }

    @After
    public void tearDown() {
        ResourceMetadataRegistry.clearBlockHandlerMap();
        ResourceMetadataRegistry.clearFallbackMap();
        ResourceMetadataRegistry.clearDefaultFallbackMap();
    }

    @Test
    public void testUpdateThenLookupFallback() throws NoSuchMethodException {
        Class<?> clazz = FooService.class;
        Method originMethod = clazz.getDeclaredMethod("fooWithFallback", int.class);
        String methodName = "fooFallbackFunc";
        Method handlerMethod = clazz.getDeclaredMethod(methodName, int.class);
        assertThat(ResourceMetadataRegistry.lookupFallback(originMethod, clazz, methodName)).isNull();

        ResourceMetadataRegistry.updateFallbackFor(originMethod, clazz, methodName, null);
        assertThat(ResourceMetadataRegistry.lookupFallback(originMethod, clazz, methodName).isPresent()).isFalse();

        ResourceMetadataRegistry.updateFallbackFor(originMethod, clazz, methodName, handlerMethod);
        MethodWrapper wrapper = ResourceMetadataRegistry.lookupFallback(originMethod, clazz, methodName);
        assertThat(wrapper.isPresent()).isTrue();
        assertThat(wrapper.getMethod()).isSameAs(handlerMethod);
    }

    @Test
    public void testUpdateThenLookupBlockHandler() throws NoSuchMethodException {
        Class<?> clazz = FooService.class;
        Method originMethod = clazz.getDeclaredMethod("foo", int.class);
        String methodName = "fooBlockHandler";
        Method handlerMethod = clazz.getDeclaredMethod(methodName, int.class, BlockException.class);
        assertThat(ResourceMetadataRegistry.lookupBlockHandler(originMethod, clazz, methodName)).isNull();

        ResourceMetadataRegistry.updateBlockHandlerFor(originMethod, clazz, methodName, null);
        assertThat(ResourceMetadataRegistry.lookupBlockHandler(originMethod, clazz, methodName).isPresent()).isFalse();

        ResourceMetadataRegistry.updateBlockHandlerFor(originMethod, clazz, methodName, handlerMethod);
        MethodWrapper wrapper = ResourceMetadataRegistry.lookupBlockHandler(originMethod, clazz, methodName);
        assertThat(wrapper.isPresent()).isTrue();
        assertThat(wrapper.getMethod()).isSameAs(handlerMethod);
    }

    @Test
    public void testUpdateThenLookupDefaultFallback() throws NoSuchMethodException {
        Class<?> clazz = FooService.class;
        Method originMethod = clazz.getDeclaredMethod("anotherFoo", int.class);
        String methodName = "globalDefaultFallback";
        Class<?> handlerClass = FooUtil.class;
        Method handlerMethod = handlerClass.getDeclaredMethod(methodName, Throwable.class);
        assertThat(ResourceMetadataRegistry.lookupDefaultFallback(originMethod, clazz, methodName)).isNull();

        ResourceMetadataRegistry.updateDefaultFallbackFor(originMethod, clazz, methodName, null);
        assertThat(ResourceMetadataRegistry.lookupDefaultFallback(originMethod, clazz, methodName).isPresent()).isFalse();

        ResourceMetadataRegistry.updateDefaultFallbackFor(originMethod, clazz, methodName, handlerMethod);
        MethodWrapper wrapper = ResourceMetadataRegistry.lookupDefaultFallback(originMethod, clazz, methodName);
        assertThat(wrapper.isPresent()).isTrue();
        assertThat(wrapper.getMethod()).isSameAs(handlerMethod);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateBlockHandlerBadArgument() {
        ResourceMetadataRegistry.updateBlockHandlerFor(null, null, "sxs", String.class.getMethods()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFallbackBadArgument() {
        ResourceMetadataRegistry.updateFallbackFor(null, String.class, "", String.class.getMethods()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDefaultFallbackBadArgument() {
        ResourceMetadataRegistry.updateDefaultFallbackFor(null, String.class, "", String.class.getMethods()[0]);
    }
}
