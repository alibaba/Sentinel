/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.annotation.cdi.interceptor;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

/**
 * @author sea
 */
public class DemoApplication {

    public static void main(String[] args) {
        SeContainerInitializer containerInit = SeContainerInitializer.newInstance();
        SeContainer container = containerInit.initialize();

        TestService testService = container.select(TestService.class).get();

        testService.test();

        System.out.println(testService.hello(-1));
        System.out.println(testService.hello(1));

        System.out.println(testService.helloAnother("bad"));

        try {
            System.out.println(testService.helloAnother("foo"));
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        }

        System.out.println(testService.helloAnother("weld"));

        container.close();
        System.exit(0);
    }
}
