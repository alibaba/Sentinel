/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.spring.webmvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Test controller
 *
 * @author kaizi2009
 */
@Controller
public class WebMvcTestController {

    @GetMapping("/async")
    public Callable<String> apiAsync() {
        return () -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return "callable";
        };
    }

    @GetMapping("/async2")
    @ResponseBody
    public Callable<String> apiAsync2() {
        return () -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return "callable";
        };
    }

    @GetMapping("/callable")
    public Callable<String> apiCallable() {
        return () -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return "callable2";
        };
    }

    @GetMapping("/callable2")
    public Callable<String> apiCallable2() {
        return () -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return "calculate";
        };
    }

    @GetMapping("/calculate")
    @ResponseBody
    public String apiCalculate() {
        int a = 1 / 0;
        return "success";
    }

    @GetMapping("/deferred")
    @ResponseBody
    public DeferredResult<String> apiDeferred() {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        CompletableFuture.supplyAsync(() -> {
            return "this is deferred result";
        }).whenCompleteAsync((res, t) -> {
            deferredResult.setResult(res);
        });
        return deferredResult;
    }

    @GetMapping("/hello")
    @ResponseBody
    public String apiHello() {
        doBusiness();
        return "Hello!";
    }

    @GetMapping("/err")
    @ResponseBody
    public String apiError() {
        doBusiness();
        return "Oops...";
    }

    @GetMapping("/foo/{id}")
    @ResponseBody
    public String apiFoo(@PathVariable("id") Long id) {
        doBusiness();
        return "Hello " + id;
    }

    @GetMapping("/exclude/{id}")
    @ResponseBody
    public String apiExclude(@PathVariable("id") Long id) {
        doBusiness();
        return "Exclude " + id;
    }

    @GetMapping("/forward")
    public ModelAndView apiForward() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("hello");
        return mav;
    }

    private void doBusiness() {
        Random random = new Random(1);
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
