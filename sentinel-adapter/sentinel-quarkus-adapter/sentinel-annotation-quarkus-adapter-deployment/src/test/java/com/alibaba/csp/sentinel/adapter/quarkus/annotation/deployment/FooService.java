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
package com.alibaba.csp.sentinel.adapter.quarkus.annotation.deployment;
import com.alibaba.csp.sentinel.annotation.cdi.interceptor.SentinelResourceBinding;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Eric Zhao
 * @author sea
 */
@ApplicationScoped
public class FooService {

    @SentinelResourceBinding(value = "apiFoo", blockHandler = "fooBlockHandler",
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

    @SentinelResourceBinding(value = "apiFooWithFallback", blockHandler = "fooBlockHandler", fallback = "fooFallbackFunc",
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

    @SentinelResourceBinding(value = "apiAnotherFooWithDefaultFallback", defaultFallback = "globalDefaultFallback",
        fallbackClass = {FooUtil.class})
    public String anotherFoo(int i) {
        if (i == 5758) {
            throw new IllegalArgumentException("oops");
        }
        return "Hello for " + i;
    }

    @SentinelResourceBinding(blockHandler = "globalBlockHandler", blockHandlerClass = FooUtil.class)
    public int random() {
        return ThreadLocalRandom.current().nextInt(0, 30000);
    }

    @SentinelResourceBinding(value = "apiBaz", blockHandler = "bazBlockHandler",
            exceptionsToIgnore = {IllegalMonitorStateException.class})
    public String baz(String name) {
        if (name.equals("fail")) {
            throw new IllegalMonitorStateException("boom!");
        }
        return "cheers, " + name;
    }

    public String fooBlockHandler(int i, BlockException ex) {
        return "Oops, " + i;
    }

    public String fooFallbackFunc(int i) {
        return "eee...";
    }
}
