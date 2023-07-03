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
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Make exception visible to Sentinel.SentinelExceptionAware should be front of ExceptionHandlerExceptionResolver
 * whose order is 0 {@link  org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver}
 *
 * @author lemonJ
 */
@Order(-1)
public class SentinelExceptionAware implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        addExceptionToRequest(request, ex);
        return null;
    }

    private void addExceptionToRequest(HttpServletRequest httpServletRequest, Exception exception) {
        if(BlockException.isBlockException(exception)){
            return;
        }
        httpServletRequest.setAttribute(BaseWebMvcConfig.REQUEST_REF_EXCEPTION_NAME, exception);
    }
}
