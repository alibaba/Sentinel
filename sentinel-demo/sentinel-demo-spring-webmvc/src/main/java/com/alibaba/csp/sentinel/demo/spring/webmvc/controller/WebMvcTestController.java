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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Test controller
 * @author kaizi2009
 */
@Controller
public class WebMvcTestController {

    @GetMapping("/metric")
    @ResponseBody
    public Map<String, Object> apiQps(String url){
        ClusterNode cn = ClusterBuilderSlot.getClusterNode("GET:/" + url);
        if (cn == null) {
            return new HashMap<>();
        }
        cn.totalPass();
        cn.totalSuccess();
        Map<String, Object> res = new HashMap<>();
        res.put("totalPass", cn.totalPass());
        res.put("totalSuccess", cn.totalSuccess());
        return res;
    }

    @GetMapping("/async")
    @ResponseBody
    public Callable<String> apiAsync(){
        return ()->{
            TimeUnit.MILLISECONDS.sleep(500);
            return "apiAsync!";
        };
    }

    @GetMapping("/nestedAsync")
    public Callable<String> apiNestedAsync(){
        return ()->{
            TimeUnit.MILLISECONDS.sleep(500);
            return "async";
        };
    }

    @GetMapping("/async2Sync")
    public Callable<String> apiAsync2Sync(){
        return ()->{
            TimeUnit.MILLISECONDS.sleep(500);
            return "sync";
        };
    }

    @GetMapping("/async2Sync2Async")
    public Callable<String> apiAsync2Sync2Async(){
        return ()->{
            TimeUnit.MILLISECONDS.sleep(500);
            return "sync2Async";
        };
    }

    @GetMapping("/sync")
    @ResponseBody
    public String apiSync() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
        return "sync!";
    }

    @GetMapping("/sync2Async")
    public String apiSync2Async() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
        return "async";
    }

    @GetMapping("/sync2Exception")
    public String apiSync2Exception() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
        return "exception";
    }

    @GetMapping("/async2Exception")
    public Callable<String> apiAsync2Exception() {
        return ()->{
            TimeUnit.MILLISECONDS.sleep(500);
            return "exception";
        };
    }

    @GetMapping("/exception")
    @ResponseBody
    public String apiException() {
        int a = 1 / 0;
        return "successful!";
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
