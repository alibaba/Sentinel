/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.Callable;

/**
 * @author kaizi2009
 */
@Controller
public class TestController {


    @GetMapping("/async")
    @ResponseBody
    public Callable<String> apiAsync(){
        return ()->{
            return "apiAsync!";
        };
    }

    @GetMapping("/nestedAsync")
    public Callable<String> apiNestedAsync(){
        return ()->{
            return "async";
        };
    }

    @GetMapping("/async2Sync")
    public Callable<String> apiAsync2Sync(){
        return ()->{
            return "sync";
        };
    }

    @GetMapping("/async2Sync2Async")
    public Callable<String> apiAsync2Sync2Async(){
        return ()->{
            return "sync2Async";
        };
    }

    @GetMapping("/sync")
    @ResponseBody
    public String apiSync() throws InterruptedException {
        return "sync!";
    }

    @GetMapping("/sync2Async")
    public String apiSync2Async() throws InterruptedException {
        return "async";
    }

    @GetMapping("/sync2Exception")
    public String apiSync2Exception() throws InterruptedException {
        return "runtimeException";
    }

    @GetMapping("/async2Exception")
    public Callable<String> apiAsync2Exception() {
        return ()->{
            return "runtimeException";
        };
    }


    @GetMapping("/hello")
    @ResponseBody
    public String apiHello() {
        return "Hello!";
    }

    @GetMapping("/err")
    @ResponseBody
    public String apiError() {
        return "Oops...";
    }

    @GetMapping("/foo/{id}")
    @ResponseBody
    public String apiFoo(@PathVariable("id") Long id) {
        return "foo " + id;
    }

    @GetMapping("/runtimeException")
    @ResponseBody
    public String runtimeException() {
        int i = 1 / 0;
        return "runtimeException";
    }

    @GetMapping("/exclude/{id}")
    @ResponseBody
    public String apiExclude(@PathVariable("id") Long id) {
        return "Exclude " + id;
    }

}
