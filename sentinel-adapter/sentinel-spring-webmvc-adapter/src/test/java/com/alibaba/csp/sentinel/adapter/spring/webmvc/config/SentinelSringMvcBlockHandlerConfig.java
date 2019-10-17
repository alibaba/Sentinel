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
 * Config BlockException handler
 * @author zhangkai
 */
@ControllerAdvice
@Order(0)
public class SentinelSringMvcBlockHandlerConfig {
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
}
