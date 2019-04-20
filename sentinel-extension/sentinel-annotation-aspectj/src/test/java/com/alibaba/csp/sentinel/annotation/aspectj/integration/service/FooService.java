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

    @SentinelResource(value = "apiFoo", blockHandler = "fooBlockHandler", fallback = "fooFallbackFunc",
        exceptionsToTrace = {IllegalArgumentException.class})
    public String foo(int i) throws Exception {
        if (i == 9527) {
            throw new DegradeException("ggg");
        }
        if (i == 5758) {
            throw new IllegalAccessException();
        }
        if (i == 5763) {
            throw new IllegalArgumentException();
        }
        return "Hello for " + i;
    }

    @SentinelResource(blockHandler = "globalBlockHandler", blockHandlerClass = FooUtil.class)
    public int random() {
        return ThreadLocalRandom.current().nextInt(0, 30000);
    }

    @SentinelResource(value = "apiBaz", blockHandler = "bazBlockHandler")
    public String baz(String name) {
        return "cheers, " + name;
    }

    public String fooBlockHandler(int i, BlockException ex) {
        return "Oops, " + i;
    }

    public String fooFallbackFunc(int i) {
        return "eee...";
    }
}
