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
package com.alibaba.csp.sentinel.demo.apache.dubbo.consumer;

import com.alibaba.csp.sentinel.demo.apache.dubbo.FooService;
import org.apache.dubbo.config.annotation.Reference;

/**
 * @author Eric Zhao
 */
public class FooServiceConsumer {

    @Reference(url = "dubbo://127.0.0.1:25758", timeout = 500)
    private FooService fooService;

    public String sayHello(String name) {
        return fooService.sayHello(name);
    }

    public String doAnother() {
        return fooService.doAnother();
    }

    public String exceptionTest(boolean biz, boolean timeout) {
        return fooService.exceptionTest(biz, timeout);
    }
}
