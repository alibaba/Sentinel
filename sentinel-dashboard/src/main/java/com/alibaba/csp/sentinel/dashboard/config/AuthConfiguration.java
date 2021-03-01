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
package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.auth.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthConfiguration {

    private final AuthProperties authProperties;

    public AuthConfiguration(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthService<HttpServletRequest> httpServletRequestAuthService() {
        if (this.authProperties.isEnabled()) {
            return new SimpleWebAuthServiceImpl();
        }
        return new FakeAuthServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginAuthenticationFilter loginAuthenticationFilter(AuthService<HttpServletRequest> httpServletRequestAuthService) {
        return new DefaultLoginAuthenticationFilter(httpServletRequestAuthService);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationInterceptor authorizationInterceptor(AuthService<HttpServletRequest> httpServletRequestAuthService) {
        return new DefaultAuthorizationInterceptor(httpServletRequestAuthService);
    }

}
