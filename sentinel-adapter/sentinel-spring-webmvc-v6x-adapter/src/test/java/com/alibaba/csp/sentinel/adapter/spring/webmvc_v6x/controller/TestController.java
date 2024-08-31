/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.controller;


import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.exception.BizException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author kaizi2009
 */
@RestController
public class TestController {

    @GetMapping("/hello")
    public String apiHello() {
        return "Hello!";
    }

    @GetMapping("/err")
    public String apiError() {
        return "Oops...";
    }

    @GetMapping("/foo/{id}")
    public String apiFoo(@PathVariable("id") Long id) {
        return "foo " + id;
    }

    @GetMapping("/runtimeException")
    public String runtimeException() {
        int i = 1 / 0;
        return "runtimeException";
    }

    @GetMapping("/bizException")
    public String bizException() {
        throw new BizException();
    }

    @GetMapping("/exclude/{id}")
    public String apiExclude(@PathVariable("id") Long id) {
        return "Exclude " + id;
    }

    @GetMapping("/async")
    @ResponseBody
    public DeferredResult<String> distribute() throws Exception {
        DeferredResult<String> result = new DeferredResult<>();

        Thread thread = new Thread(() -> result.setResult("async result."));
        thread.start();

        Thread.yield();
        return result;
    }

}
