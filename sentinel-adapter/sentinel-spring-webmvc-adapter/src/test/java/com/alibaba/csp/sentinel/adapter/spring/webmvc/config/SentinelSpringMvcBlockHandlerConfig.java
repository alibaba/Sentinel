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
package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.ResultWrapper;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Config 'BlockException' handler, handler it in spring veb 'ExceptionHandler'
 *
 * @author kaizi2009
 */
@ControllerAdvice
@Order(0)
public class SentinelSpringMvcBlockHandlerConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public ResultWrapper sentinelBlockHandler(BlockException e) {
        AbstractRule rule = e.getRule();
        //Log
        logger.info("Blocked by sentinel, {}", rule.toString());
        //Return object
        return ResultWrapper.blocked();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultWrapper exceptionHandler(Exception e) {
        logger.error("System error", e.getMessage());
        return new ResultWrapper(-1, "System error");
    }
}
