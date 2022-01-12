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
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.demo.spring.webmvc.vo.ResultWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring configuration for global exception handler.
 * This will be activated when the {@code BlockExceptionHandler}
 * throws {@link BlockException directly}.
 *
 * @author kaizi2009
 */
@ControllerAdvice
@Order(0)
public class SentinelSpringMvcBlockHandlerConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static SentinelWebInterceptor sentinelWebInterceptor;

    static {
        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setBlockExceptionHandler(new DefaultBlockExceptionHandler());
        config.setHttpMethodSpecify(true);
        config.setWebContextUnify(true);
        config.setOriginParser(request -> request.getHeader("S-user"));
        sentinelWebInterceptor = new SentinelWebInterceptor(config);
    }

    /**
     * Global exception or a business exception to manually call sentinelWebInterceptor.exceptionControllerAdviceExit(req,e); Do exception statistics
     * Otherwise, Sentinel cannot perform exception statistics and exception degrade
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultWrapper exceptionHandler(HttpServletRequest req, Exception e) {
        logger.warn("System Exception: ", e);
        sentinelWebInterceptor.exceptionControllerAdviceExit(req,e);
        return ResultWrapper.systemException();
    }

    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public ResultWrapper sentinelBlockHandler(BlockException e) {
        logger.warn("Blocked by Sentinel: {}", e.getRule());
        // Return the customized result.
        return ResultWrapper.blocked();
    }
}
