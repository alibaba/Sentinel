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
package com.alibaba.csp.sentinel.adapter.spring.webflux.test;

import com.alibaba.csp.sentinel.adapter.spring.webflux.support.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.WebSessionManager;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author icodening
 * @date 2022.03.30
 */
public class BestMatchingPatternExtractTest {

    /**
     * some request path
     */
    private static final String BASE_PATH = "/users";
    private static final String PATH_123 = BASE_PATH + "/123";
    private static final String PATH_456 = BASE_PATH + "/456";


    /**
     * some "HandlerMapping"
     */
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private RouterFunctionMapping routerFunctionMapping;


    /**
     * some base constructor params for DefaultServerWebExchange
     */
    private final WebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
    private final ServerCodecConfigurer serverCodecConfigurer = ServerCodecConfigurer.create();
    private final LocaleContextResolver acceptHeaderLocaleContextResolver = new AcceptHeaderLocaleContextResolver();


    /**
     * some ServerWebExchange, can be verify HandlerMappingBestMatchingPatternExtractor
     */
    private ServerWebExchange getUserByIdExchange1;
    private ServerWebExchange getUserByIdExchange2;
    private ServerWebExchange postGetUserByIdExchange2;


    @Before
    public void setup() throws Exception {
        initRequestMappingHandlerMapping();
        initRouterFunctionMapping();
        buildMockServerWebExchange();
    }

    @Test
    public void testBestMatchingByRequestMappingHandlerMapping() {
        //1.get HandlerMappingBestMatchingPatternExtractor instance
        HandlerMappingBestMatchingPatternExtractor handlerMappingBestMatchingPatternExtractor = getRequestMappingHandlerMappingBestMatchingExtractor();
        //2.extract HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE
        String bestMatchingPattern1 = handlerMappingBestMatchingPatternExtractor.extract(requestMappingHandlerMapping, getUserByIdExchange1);
        String bestMatchingPattern2 = handlerMappingBestMatchingPatternExtractor.extract(requestMappingHandlerMapping, getUserByIdExchange2);
        String bestMatchingPattern3 = handlerMappingBestMatchingPatternExtractor.extract(requestMappingHandlerMapping, postGetUserByIdExchange2);
        System.out.println("bestMatchingPattern1 = " + bestMatchingPattern1);
        System.out.println("bestMatchingPattern2 = " + bestMatchingPattern2);
        System.out.println("bestMatchingPattern3 = " + bestMatchingPattern3);
        //3.assert
        assertEquals("/users/{id}", bestMatchingPattern1);
        assertEquals("/users/{id}", bestMatchingPattern2);
        assertNotEquals("/users/{id}", bestMatchingPattern3);
        assertEquals(bestMatchingPattern1, bestMatchingPattern2);
    }

    @Test
    public void testBestMatchingByRouterFunctionMapping() {
        //1.get HandlerMappingBestMatchingPatternExtractor instance
        HandlerMappingBestMatchingPatternExtractor handlerMappingBestMatchingPatternExtractor = getRouterFunctionBestMatchingPatternExtractor();
        //2.extract HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE
        String bestMatchingPattern1 = handlerMappingBestMatchingPatternExtractor.extract(routerFunctionMapping, getUserByIdExchange1);
        String bestMatchingPattern2 = handlerMappingBestMatchingPatternExtractor.extract(routerFunctionMapping, getUserByIdExchange2);
        String bestMatchingPattern3 = handlerMappingBestMatchingPatternExtractor.extract(routerFunctionMapping, postGetUserByIdExchange2);
        System.out.println("bestMatchingPattern1 = " + bestMatchingPattern1);
        System.out.println("bestMatchingPattern2 = " + bestMatchingPattern2);
        System.out.println("bestMatchingPattern3 = " + bestMatchingPattern3);
        //3.assert
        assertEquals("/users/{id}", bestMatchingPattern1);
        assertEquals("/users/{id}", bestMatchingPattern2);
        assertNotEquals("/users/{id}", bestMatchingPattern3);
        assertEquals(bestMatchingPattern1, bestMatchingPattern2);
    }

    @Test
    public void testInvokeHandler() {
        ServerWebExchange selectedServerWebExchange = getUserByIdExchange2;

        //test invoke with RequestMappingHandlerMapping
        requestMappingHandlerMapping.getHandler(selectedServerWebExchange)
                .filter(HandlerMethod.class::isInstance)
                .doOnNext(handler -> {
                    Map<String, String> pathVariables = selectedServerWebExchange.getAttributeOrDefault(
                            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.emptyMap());
                    Method method = ((HandlerMethod) handler).getMethod();
                    method.setAccessible(true);
                    ReflectionUtils.invokeMethod(method, ((HandlerMethod) handler).getBean(), Long.valueOf(pathVariables.get("id")));
                })
                .subscribe();

        //test invoke with RouterFunctionMapping
        routerFunctionMapping.getHandler(selectedServerWebExchange)
                .filter(HandlerFunction.class::isInstance)
                .doOnNext(handler -> ((HandlerFunction<?>) handler).handle(new SentinelServerRequest(selectedServerWebExchange)))
                .subscribe();
    }

    private void initRequestMappingHandlerMapping() throws Exception {
        requestMappingHandlerMapping = new RequestMappingHandlerMapping();
        UserController userController = new UserController();
        Method getUserById = UserController.class.getDeclaredMethod("getUserById", Long.class);
        requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(BASE_PATH + "/{id}").methods(RequestMethod.GET).build(),
                userController,
                getUserById);
    }

    private void initRouterFunctionMapping() {
        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route()
                .GET(BASE_PATH + "/{id}", serverRequest -> {
                    String id = serverRequest.pathVariable("id");
                    String ret = "user id = " + id + ", from RouterFunction";
                    System.out.println(ret);
                    return ServerResponse.ok().body(BodyInserters.fromObject(ret));
                })
                .build();
        routerFunctionMapping = new RouterFunctionMapping(routerFunction);
    }

    private void buildMockServerWebExchange() {
        //GET /users/123
        getUserByIdExchange1 = new DefaultServerWebExchange(MockServerHttpRequest.get(PATH_123).build(),
                new MockServerHttpResponse(),
                defaultWebSessionManager,
                serverCodecConfigurer,
                acceptHeaderLocaleContextResolver);

        //GET /users/456
        getUserByIdExchange2 = new DefaultServerWebExchange(MockServerHttpRequest.get(PATH_456).build(),
                new MockServerHttpResponse(),
                defaultWebSessionManager,
                serverCodecConfigurer,
                acceptHeaderLocaleContextResolver);

        //POST /users/456
        postGetUserByIdExchange2 = new DefaultServerWebExchange(MockServerHttpRequest.post(PATH_456).build(),
                new MockServerHttpResponse(),
                defaultWebSessionManager,
                serverCodecConfigurer,
                acceptHeaderLocaleContextResolver);
    }


    private HandlerMappingBestMatchingPatternExtractor getRequestMappingHandlerMappingBestMatchingExtractor() {
        return new RequestMappingHandlerMappingBestMatchingPatternExtractor();
    }

    private HandlerMappingBestMatchingPatternExtractor getRouterFunctionBestMatchingPatternExtractor() {
        return new RouterFunctionMappingBestMatchingPatternExtractor();
    }

    /**
     * mock controller
     */
    private static class UserController {

        public String getUserById(@PathVariable Long id) {
            String ret = "user id = " + id + ", from RequestMappingHandlerMapping";
            System.out.println(ret);
            return ret;
        }
    }
}
