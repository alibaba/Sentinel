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
package com.alibaba.csp.sentinel.demo.mybatis.config;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handle BlockException
 *
 * @@author kaizi2009
 */
@ControllerAdvice
@Order(0)
public class BlockExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(MyBatisSystemException.class)
    @ResponseBody
    public String sentinelBlockHandler(MyBatisSystemException e) {
        BlockException blockException = BlockException.getBlockException(e);
        if (blockException != null) {
            AbstractRule rule = blockException.getRule();
            logger.info("Blocked by mybatis sentinel, {}", rule.toString());
            return "Blocked by mybatis sentinel, " + rule.toString();
        } else {
            return exceptionHandler(e);
        }

    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String exceptionHandler(Exception e) {
        logger.error("System error", e.getMessage());
        return "System error";
    }
}
