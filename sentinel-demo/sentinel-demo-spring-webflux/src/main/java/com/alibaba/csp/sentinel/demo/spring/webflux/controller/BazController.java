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
package com.alibaba.csp.sentinel.demo.spring.webflux.controller;

import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import com.alibaba.csp.sentinel.demo.spring.webflux.service.BazService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Eric Zhao
 */
@RestController
@RequestMapping(value = "/baz")
public class BazController {

    @Autowired
    private BazService bazService;

    @GetMapping("/{id}")
    public Mono<String> apiGetValue(@PathVariable("id") Long id) {
        return bazService.getById(id)
            .transform(new SentinelReactorTransformer<>("BazService:getById"));
    }

    @PostMapping("/{id}")
    public Mono<Boolean> apiSetValue(@PathVariable("id") Long id, @RequestBody String value) {
        return bazService.setValue(id, value)
            .transform(new SentinelReactorTransformer<>("BazService:setValue"));
    }
}
