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
package com.alibaba.csp.sentinel.demo.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebTotalInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcTotalConfig;

import com.alibaba.csp.sentinel.context.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Config sentinel interceptor
 *
 * @author kaizi2009
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add Sentinel interceptor
        addSpringMvcInterceptor(registry);
    }

    private void addSpringMvcInterceptor(InterceptorRegistry registry) {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();

        // Depending on your situation, you can choose to process the BlockException via
        // the BlockExceptionHandler or throw it directly, then handle it
        // in Spring web global exception handler.

        // config.setBlockExceptionHandler((request, response, e) -> { throw e; });

        // Use the default handler.
        config.setBlockExceptionHandler(new DefaultBlockExceptionHandler());

        // Custom configuration if necessary
        config.setHttpMethodSpecify(true);
        // By default web context is true, means that unify web context(i.e. use the default context name),
        // in most scenarios that's enough, and it could reduce the memory footprint.
        // If set it to false, entrance contexts will be separated by different URLs,
        // which is useful to support "chain" relation flow strategy.
        // We can change it and view different result in `Resource Chain` menu of dashboard.
        config.setWebContextUnify(true);
        config.setOriginParser(request -> request.getHeader("S-user"));

        // Add sentinel interceptor
        registry.addInterceptor(new SentinelWebInterceptor(config)).addPathPatterns("/**");

        //Add sentinel interceptor call info
        registry.addInterceptor(new AsyncHandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
                logger.info("preHandle: dispatcher type {}, {}, {}, {}", request.getDispatcherType(), ContextUtil.contextSize(), request.getRequestURI(), request.getAttribute(config.getRequestRefName()));
                return true;
            }
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                            @Nullable ModelAndView modelAndView) throws Exception {

                logger.info("postHandle: dispatcher type {}, {}, {},{}", request.getDispatcherType(), ContextUtil.contextSize(), request.getRequestURI(), request.getAttribute(config.getRequestRefName()));
            }
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                        @Nullable Exception ex) throws Exception {
                logger.info("afterCompletion: dispatcher type {}, {},{},{}", request.getDispatcherType(), ContextUtil.contextSize(), request.getRequestURI(), request.getAttribute(config.getRequestRefName()));
            }

            public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
                                                       Object handler) throws Exception {
                logger.info("afterConcurrentHandlingStarted: dispatcher type {}, {},{},{}", request.getDispatcherType(), ContextUtil.contextSize(), request.getRequestURI(), request.getAttribute(config.getRequestRefName()));

            }

        }).addPathPatterns("/**");
    }

    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
        //Config
        SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();

        //Custom configuration if necessary
        config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container");
        config.setTotalResourceName("my-spring-mvc-total-url-request");

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelWebTotalInterceptor(config)).addPathPatterns("/**");

    }
}
