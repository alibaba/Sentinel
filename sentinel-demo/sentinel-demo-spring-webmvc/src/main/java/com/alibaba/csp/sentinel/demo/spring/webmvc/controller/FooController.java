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

import org.springframework.web.bind.annotation.*;

/**
 * @author zhaoyuguang
 */
@RestController
@RequestMapping(value = "/foo")
public class FooController {

    @GetMapping("/{id}")
    public String getId(@PathVariable("id") String id) {
        return "get:" + id;
    }

    @PostMapping("/{id}")
    public String postId(@PathVariable("id") String id) {
        return "post:" + id;
    }

    @GetMapping("/id0")
    public String id0() {
        return "id0";
    }

    @GetMapping("/id1")
    public String id1() {
        return "id1";
    }

    @GetMapping("/id2")
    public String id2() {
        return "id12";
    }

    @GetMapping("/ex")
    public String ex() {
        throw new RuntimeException("ex");
    }
}
