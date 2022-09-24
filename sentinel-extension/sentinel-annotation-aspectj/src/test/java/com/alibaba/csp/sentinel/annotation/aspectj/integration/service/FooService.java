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
package com.alibaba.csp.sentinel.annotation.aspectj.integration.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Eric Zhao
 */
@Service
public class FooService {

    @SentinelResource(value = "apiFoo", blockHandler = "fooBlockHandler",
        exceptionsToTrace = {IllegalArgumentException.class})
    public String foo(int i) throws Exception {
        if (i == 5758) {
            throw new IllegalAccessException();
        }
        if (i == 5763) {
            throw new IllegalArgumentException();
        }
        return "Hello for " + i;
    }

    @SentinelResource(value = "apiFooWithFallback", blockHandler = "fooBlockHandler", fallback = "fooFallbackFunc",
        exceptionsToTrace = {IllegalArgumentException.class})
    public String fooWithFallback(int i) throws Exception {
        if (i == 5758) {
            throw new IllegalAccessException();
        }
        if (i == 5763) {
            throw new IllegalArgumentException();
        }
        return "Hello for " + i;
    }

    @SentinelResource(value = "apiAnotherFooWithDefaultFallback", defaultFallback = "globalDefaultFallback",
        fallbackClass = {FooUtil.class})
    public String anotherFoo(int i) {
        if (i == 5758) {
            throw new IllegalArgumentException("oops");
        }
        return "Hello for " + i;
    }

    @SentinelResource(blockHandler = "globalBlockHandler", blockHandlerClass = FooUtil.class)
    public int random() {
        return ThreadLocalRandom.current().nextInt(0, 30000);
    }

    @SentinelResource(value = "apiBaz", blockHandler = "bazBlockHandler",
            exceptionsToIgnore = {IllegalMonitorStateException.class})
    public String baz(String name) {
        if (name.equals("fail")) {
            throw new IllegalMonitorStateException("boom!");
        }
        return "cheers, " + name;
    }

    @SentinelResource(value = "apiFooWithFallback", blockHandler = "fooBlockHandlerPrivate", fallback = "fooFallbackFuncPrivate",
            exceptionsToTrace = {IllegalArgumentException.class})
    public String fooWithPrivateFallback(int i) throws Exception {
        if (i == 5758) {
            throw new IllegalAccessException();
        }
        if (i == 5763) {
            throw new IllegalArgumentException();
        }
        return "Hello for " + i;
    }

    public String fooBlockHandler(int i, BlockException ex) {
        return "Oops, " + i;
    }

    public String fooFallbackFunc(int i) {
        return "eee...";
    }

    private String fooFallbackFuncPrivate(int i) {
        return "EEE...";
    }

    private String fooBlockHandlerPrivate(int i, BlockException ex) {
        return "Oops, " + i;
    }
}
