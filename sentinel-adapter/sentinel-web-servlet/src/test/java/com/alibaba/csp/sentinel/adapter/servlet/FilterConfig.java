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
package com.alibaba.csp.sentinel.adapter.servlet;


import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eric Zhao
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean sentinelFilterRegistration() {
        //init a config
        javax.servlet.FilterConfig config = new javax.servlet.FilterConfig() {
            
            @Override
            public ServletContext getServletContext() {
                return null;
            }
            
            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
            
            @Override
            public String getInitParameter(String name) {
                return null;
            }
            
            @Override
            public String getFilterName() {
                return "sentinelFilter";
            }
        };
        
        CommonFilter filter = new CommonFilter();
        filter.init(config);
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName(config.getFilterName());
        registration.setOrder(1);

        return registration;
    }
    
}
