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
package com.alibaba.csp.sentinel.adapter.dubbo3.provider.impl;

import com.alibaba.csp.sentinel.adapter.dubbo3.provider.DemoService;

/**
 * @author leyou
 */
public class DemoServiceImpl implements DemoService {
    public String sayHello(String name, int n) {
        return "Hello " + name + ", " + n;
    }

    @Override
    public String sayHi(String name, int n) {
        return "Hi " + name + ", " + n;
    }
}
