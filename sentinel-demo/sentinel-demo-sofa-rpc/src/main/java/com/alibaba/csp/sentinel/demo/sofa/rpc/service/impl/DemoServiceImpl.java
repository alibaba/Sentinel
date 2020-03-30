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
package com.alibaba.csp.sentinel.demo.sofa.rpc.service.impl;

import com.alibaba.csp.sentinel.demo.sofa.rpc.service.DemoService;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author cdfive
 */
public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHello(Integer index, String name, int year) {
        System.out.println("[" + index + "][Provider]receive request: " + name + "," + year);

        int sleepMs = ThreadLocalRandom.current().nextInt(50);
        try {
            TimeUnit.MILLISECONDS.sleep(sleepMs);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        return "Hello " + name + " " + year + "[" + sleepMs + "ms]";
    }
}