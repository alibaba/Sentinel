/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webflux.support;

import org.springframework.web.reactive.function.server.RequestPredicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RequestPredicate cache for RouterFunction
 *
 * @author icodening
 * @date 2022.03.29
 */
public class RouterFunctionRequestPredicateRepository {

    private final List<RequestPredicate> requestPredicates = Collections.synchronizedList(new ArrayList<>());

    public RouterFunctionRequestPredicateRepository() {
    }

    public void addRequestPredicate(RequestPredicate requestPredicate) {
        this.requestPredicates.add(requestPredicate);
    }

    public List<RequestPredicate> getRequestPredicates() {
        return requestPredicates;
    }
}
