/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.webmvc.test;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.scp.sentinel.adapter.spring.webmvc.SentinelHandlerInterceptor;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.config.SentinelSpringWebmvcConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhaoyuguang
 */
@Configuration
public class SpringWebmvcTestConfig implements WebMvcConfigurer {

    public static final String HEADER_NAME = "x-origin";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        SentinelSpringWebmvcConfig config = new SentinelSpringWebmvcConfig();
        config.setUrlCleaner(new UrlCleaner() {
            @Override
            public String clean(String originUrl) {
                if ("/foo/id2".equals(originUrl)) {
                    return "/foo/*";
                }
                if ("/foo/clean".equals(originUrl)) {
                    return null;
                }
                return originUrl;
            }
        });
        config.setOriginParser(new RequestOriginParser() {
            @Override
            public String parseOrigin(HttpServletRequest request) {
                String origin = request.getHeader(HEADER_NAME);
                return origin != null ? origin : "";
            }
        });
        config.setHttpMethodSpecify(true);
        registry.addInterceptor(new SentinelHandlerInterceptor(config)).addPathPatterns("/**");
    }
}
