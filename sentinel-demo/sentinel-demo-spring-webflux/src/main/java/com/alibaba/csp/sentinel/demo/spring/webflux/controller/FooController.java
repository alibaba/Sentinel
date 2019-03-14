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
import com.alibaba.csp.sentinel.demo.spring.webflux.service.FooService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric Zhao
 */
@RestController
@RequestMapping(value = "/foo")
public class FooController {

    @Autowired
    private FooService fooService;

    @GetMapping("/single")
    public Mono<String> apiNormalSingle() {
        return fooService.emitSingle()
            // transform the publisher here.
            .transform(new SentinelReactorTransformer<>("demo_foo_normal_single"));
    }

    @GetMapping("/flux")
    public Flux<Integer> apiNormalFlux() {
        return fooService.emitMultiple()
            .transform(new SentinelReactorTransformer<>("demo_foo_normal_flux"));
    }

    @GetMapping("/slow")
    public Mono<String> apiDoSomethingSlow(ServerHttpResponse response) {
        return fooService.doSomethingSlow();
    }
}
