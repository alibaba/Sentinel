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

import com.alibaba.csp.sentinel.util.AssertUtil;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;

/**
 * RequestPredicate registrar for RouterFunction, existing RouterFunction can be record
 *
 * @author icodening
 * @date 2022.03.29
 */
public class RequestPredicateRegistrar implements SmartInitializingSingleton {

    private final RouterFunctionRequestPredicateRepository routerFunctionRequestPredicateRepository = new RouterFunctionRequestPredicateRepository();

    private final RouterFunctionMapping routerFunctionMapping;

    public RequestPredicateRegistrar(RouterFunctionMapping routerFunctionMapping) {
        AssertUtil.notNull(routerFunctionMapping, "routerFunctionMapping cannot be null");
        this.routerFunctionMapping = routerFunctionMapping;
    }

    @Override
    public void afterSingletonsInstantiated() {
        RouterFunction<?> routerFunction = this.routerFunctionMapping.getRouterFunction();
        if (routerFunction != null) {
            routerFunction.accept(new RouterFunctionRequestPredicateRegistrarVisitor(routerFunctionRequestPredicateRepository));
        }
    }

    public RouterFunctionRequestPredicateRepository getRouterFunctionRequestPredicateRepository() {
        return routerFunctionRequestPredicateRepository;
    }
}
