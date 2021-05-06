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
package com.alibaba.csp.sentinel.annotation.aspectj.integration.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.stereotype.Service;

/**
 * @author zhaoyuguang
 */
@Service
@SentinelResource(defaultFallback = "doFallback", fallbackClass = GlobalFallback.class)
public class BarService {

    @SentinelResource(value = "apiAnotherBarWithDefaultFallback", defaultFallback = "fallbackFunc")
    public String anotherBar(int i) {
        if (i == 5758) {
            throw new IllegalArgumentException("oops");
        }
        return "Hello for " + i;
    }

    @SentinelResource()
    public String doSomething(int i) {
        if (i == 5758) {
            throw new IllegalArgumentException("oops");
        }
        return "do something";
    }

    public String fallbackFunc(Throwable t) {
        System.out.println(t.getMessage());
        return "eee...";
    }
}
