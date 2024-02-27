/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.SentinelWebTotalInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Config sentinel interceptor
 *
 * @author kaizi2009
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //Add sentinel interceptor
        addSpringMvcInterceptor(registry);

        //If you want to sentinel the total flow, you can add total interceptor
        addSpringMvcTotalInterceptor(registry);
    }

    private void addSpringMvcInterceptor(InterceptorRegistry registry) {
        //Config
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();

        config.setBlockExceptionHandler(new BlockExceptionHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, String resourceName, BlockException e) throws Exception {
                resourceName = e.getRule().getResource();
                //Depending on your situation, you can choose to process or throw
                if ("/hello".equals(resourceName)) {
                    //Do something ......
                    //Write string or json string;
                    response.getWriter().write("/Blocked by sentinel");
                } else {
                    //Handle in global exception handling
                    throw e;
                }
            }
        });

        //Custom configuration if necessary
        config.setHttpMethodSpecify(false);
        config.setWebContextUnify(true);
        config.setOriginParser(new RequestOriginParser() {
            @Override
            public String parseOrigin(HttpServletRequest request) {
                return request.getHeader("S-user");
            }
        });

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelWebInterceptor(config)).addPathPatterns("/**");
    }

    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
        //Configure
        SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();

        //Custom configuration if necessary
        config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container");
        config.setTotalResourceName("my_spring_mvc_total_url_request");

        //Add sentinel interceptor
        registry.addInterceptor(new SentinelWebTotalInterceptor(config)).addPathPatterns("/**");
    }
}
