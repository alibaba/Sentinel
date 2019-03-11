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
package com.alibaba.csp.sentinel.adapter.spring.webflux.test;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric Zhao
 */
@RestController
public class WebFluxTestController {

    @GetMapping("/hello")
    public String apiHello() {
        return "Hello!";
    }

    @GetMapping("/flux")
    public Flux<Integer> apiFlux() {
        return Flux.range(0, 5);
    }

    @GetMapping("/error")
    public Mono<?> apiError() {
        return Mono.error(new FlowException("testWebFluxError"));
    }

    @GetMapping("/foo/{id}")
    public String apiFoo(@PathVariable("id") Long id) {
        return "Hello " + id;
    }
}
